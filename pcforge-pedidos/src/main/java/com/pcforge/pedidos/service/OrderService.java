package com.pcforge.pedidos.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pcforge.pedidos.client.CatalogoClient;
import com.pcforge.pedidos.client.CompatibilidadClient;
import com.pcforge.pedidos.domain.Order;
import com.pcforge.pedidos.domain.OrderStatus;
import com.pcforge.pedidos.exception.AccessDeniedForUserException;
import com.pcforge.pedidos.exception.BusinessRuleException;
import com.pcforge.pedidos.exception.ResourceNotFoundException;
import com.pcforge.pedidos.repository.OrderRepository;
import com.pcforge.pedidos.web.dto.AddObservationRequest;
import com.pcforge.pedidos.web.dto.ChangeStatusRequest;
import com.pcforge.pedidos.web.dto.CreateOrderRequest;
import com.pcforge.pedidos.web.dto.OrderResponse;

/**
 * Cotizaciones, pedidos y seguimiento del ensamblaje.
 *
 * Las transiciones de estado estan controladas de forma explicita: no
 * cualquier perfil puede mover un pedido a cualquier estado, y el avance del
 * proceso queda registrado en un historial que nunca se sobrescribe.
 */
@Service
public class OrderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);
    /**
     * Transiciones permitidas. Se declaran como un mapa para que la regla sea
     * legible de un vistazo y no quede repartida en condiciones sueltas.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(OrderStatus.RECIBIDO, Set.of(OrderStatus.EN_REVISION, OrderStatus.CANCELADO), OrderStatus.EN_REVISION, Set.of(OrderStatus.COTIZADO, OrderStatus.CANCELADO), OrderStatus.COTIZADO, Set.of(OrderStatus.EN_ENSAMBLAJE, OrderStatus.CANCELADO), OrderStatus.EN_ENSAMBLAJE, Set.of(OrderStatus.EN_PRUEBAS, OrderStatus.CANCELADO), OrderStatus.EN_PRUEBAS, Set.of(OrderStatus.EN_ENSAMBLAJE, OrderStatus.LISTO_PARA_ENTREGA, OrderStatus.CANCELADO), OrderStatus.LISTO_PARA_ENTREGA, Set.of(OrderStatus.ENTREGADO, OrderStatus.CANCELADO), OrderStatus.ENTREGADO, Set.of(), OrderStatus.CANCELADO, Set.of());
    private final OrderRepository orderRepository;
    private final SequenceService sequenceService;
    private final CompatibilidadClient compatibilidadClient;
    private final CatalogoClient catalogoClient;

    // ---------------------------------------------------------------- registro
    /**
     * Registra una solicitud a partir de una configuracion armada.
     * Los componentes y los precios se toman del catalogo vigente: el cuerpo
     * de la peticion no puede imponer un precio.
     */
    @Transactional
    public OrderResponse create(Caller caller, CreateOrderRequest request) {
        CompatibilidadClient.CompatibilidadConfiguration configuration = compatibilidadClient.findConfiguration(request.configurationId(), caller.bearerToken());
        if (configuration.items() == null || configuration.items().isEmpty()) {
            throw new BusinessRuleException("La configuracion no tiene componentes; no se puede generar una solicitud");
        }
        List<Order.OrderItem> items = new ArrayList<>();
        List<String> insufficient = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CompatibilidadClient.CompatibilidadConfiguration.Item item : configuration.items()) {
            CatalogoClient.CatalogoProduct product = catalogoClient.findProduct(item.productId());
            if (!product.active()) {
                throw new BusinessRuleException("El producto " + product.sku() + " esta inactivo y no puede formar parte de un pedido");
            }
            if (product.stock() < item.quantity()) {
                insufficient.add(product.sku() + " (disponible " + product.stock() + ", requerido " + item.quantity() + ")");
            }
            BigDecimal unitPrice = product.price() != null ? product.price() : item.price();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));
            subtotal = subtotal.add(lineTotal);
            items.add(Order.OrderItem.builder().productId(product.id()).sku(product.sku()).name(product.name()).componentType(product.componentType()).unitPrice(unitPrice).quantity(item.quantity()).lineTotal(lineTotal).build());
        }
        // La disponibilidad se vuelve a comprobar aqui porque lo que se vio en
        // pantalla pudo cambiar entre el armado y el registro.
        if (!insufficient.isEmpty()) {
            throw new BusinessRuleException("No hay existencias suficientes para completar la solicitud: " + String.join("; ", insufficient));
        }
        Order order = Order.builder().orderNumber(sequenceService.nextOrderNumber()).customerId(caller.userId()).customerEmail(caller.email()).customerName(caller.name()).configurationId(configuration.id()).compatibilityResult(configuration.lastResult()).items(items).subtotal(subtotal).total(subtotal).customerNote(request.customerNote()).status(OrderStatus.RECIBIDO).build();
        order.getHistory().add(statusChange(null, OrderStatus.RECIBIDO, caller, "Solicitud registrada"));
        return respond(orderRepository.save(order));
    }

    // ---------------------------------------------------------------- cambios
    @Transactional
    public OrderResponse changeStatus(String orderId, ChangeStatusRequest request, Caller caller) {
        Order order = requireAccessible(orderId, caller);
        OrderStatus from = order.getStatus();
        OrderStatus to = request.status();
        if (from == to) {
            throw new BusinessRuleException("El pedido ya se encuentra en el estado " + to);
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new BusinessRuleException("No se puede pasar de " + from + " a " + to + ". Estados posibles desde " + from + ": " + ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()));
        }
        assertRoleForTarget(to, order, caller);
        if (request.technicianId() != null && !request.technicianId().isBlank()) {
            if (!caller.isCommercialStaff()) {
                throw new AccessDeniedForUserException("Solo el asesor comercial o el administrador pueden asignar un tecnico");
            }
            order.setAssignedTechnicianId(request.technicianId());
            order.setAssignedTechnicianName(request.technicianName());
        }
        // Al entrar a la linea de armado se descuentan las existencias.
        if (to == OrderStatus.EN_ENSAMBLAJE && !order.isStockDiscounted()) {
            discountStock(order, caller);
        }
        order.setStatus(to);
        if (to == OrderStatus.ENTREGADO) {
            order.setDeliveredAt(Instant.now());
        }
        order.getHistory().add(statusChange(from, to, caller, request.note()));
        return respond(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse addObservation(String orderId, AddObservationRequest request, Caller caller) {
        Order order = requireAccessible(orderId, caller);
        if (order.getStatus().isClosed()) {
            throw new BusinessRuleException("El pedido esta " + order.getStatus() + " y ya no admite observaciones");
        }
        order.getObservations().add(Order.Observation.builder().authorId(caller.userId()).authorName(caller.name()).authorRole(String.join(", ", caller.roles() == null ? List.of() : caller.roles())).text(request.text().trim()).createdAt(Instant.now()).build());
        return respond(orderRepository.save(order));
    }

    // -------------------------------------------------------------- consultas
    public List<OrderResponse> list(Caller caller, OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtAsc(status);
        } else if (caller.canSeeAllOrders()) {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        } else {
            orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(caller.userId());
        }
        return orders.stream().filter(order -> caller.canSeeAllOrders() || Objects.equals(order.getCustomerId(), caller.userId())).map(this::respond).toList();
    }

    /**
     * Acepta el identificador interno o el numero legible del pedido.
     */
    public OrderResponse find(String idOrNumber, Caller caller) {
        Order order = orderRepository.findById(idOrNumber).or(() -> orderRepository.findByOrderNumber(idOrNumber)).orElseThrow(() -> new ResourceNotFoundException("No existe el pedido " + idOrNumber));
        assertAccessible(order, caller);
        return respond(order);
    }

    // ------------------------------------------------------------- inventario
    /**
     * Descuenta las existencias de cada componente. Si algun descuento falla,
     * se devuelven los que ya se habian descontado: sin transaccion
     * distribuida, la unica alternativa seria dejar el inventario a medias.
     */
    private void discountStock(Order order, Caller caller) {
        List<Order.OrderItem> discounted = new ArrayList<>();
        try {
            for (Order.OrderItem item : order.getItems()) {
                int remaining = catalogoClient.registerExit(item.getProductId(), item.getQuantity(), "Pedido " + order.getOrderNumber(), caller.bearerToken());
                discounted.add(item);
                log.info("Existencia descontada: {} x{} del pedido {}; quedan {} unidades", item.getSku(), item.getQuantity(), order.getOrderNumber(), remaining);
            }
            order.setStockDiscounted(true);
        } catch (RuntimeException ex) {
            log.error("Fallo el descuento de existencias del pedido {}; se devuelven los ya descontados", order.getOrderNumber(), ex);
            for (Order.OrderItem item : discounted) {
                try {
                    catalogoClient.registerEntry(item.getProductId(), item.getQuantity(), "Reversion por fallo al iniciar el pedido " + order.getOrderNumber(), caller.bearerToken());
                } catch (RuntimeException reversionFailure) {
                    log.error("No fue posible revertir las existencias del producto {}", item.getSku(), reversionFailure);
                }
            }
            throw new BusinessRuleException("No se pudo iniciar el ensamblaje porque el inventario no pudo actualizarse: " + ex.getMessage(), ex);
        }
    }

    // ----------------------------------------------------------------- apoyo
    private OrderResponse respond(Order order) {
        return OrderResponse.from(order, List.copyOf(ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), Set.of())));
    }

    private Order requireAccessible(String id, Caller caller) {
        Order order = orderRepository.findById(id).or(() -> orderRepository.findByOrderNumber(id)).orElseThrow(() -> new ResourceNotFoundException("No existe el pedido " + id));
        assertAccessible(order, caller);
        return order;
    }

    private void assertAccessible(Order order, Caller caller) {
        if (!caller.canSeeAllOrders() && !Objects.equals(order.getCustomerId(), caller.userId())) {
            throw new AccessDeniedForUserException("El pedido pertenece a otro cliente");
        }
    }

    /**
     * Cada estado destino exige un perfil determinado.
     */
    private void assertRoleForTarget(OrderStatus to, Order order, Caller caller) {
        switch (to) {
            case EN_REVISION, COTIZADO, EN_ENSAMBLAJE, ENTREGADO -> {
                if (!caller.isCommercialStaff()) {
                    throw new AccessDeniedForUserException("Solo el asesor comercial o el administrador pueden mover el pedido a " + to);
                }
            }
            case EN_PRUEBAS, LISTO_PARA_ENTREGA -> {
                if (!caller.isTechnician() && !caller.isAdmin()) {
                    throw new AccessDeniedForUserException("Solo el tecnico de ensamblaje o el administrador pueden mover el pedido a " + to);
                }
            }
            case CANCELADO -> {
                boolean ownerWhileEarly = Objects.equals(order.getCustomerId(), caller.userId()) && order.getStatus().isCancellableByCustomer();
                if (!caller.isCommercialStaff() && !ownerWhileEarly) {
                    throw new AccessDeniedForUserException("Solo el asesor, el administrador, o el propio cliente mientras la solicitud " + "este en revision pueden anular el pedido");
                }
            }
            default -> {
                // RECIBIDO no es un estado destino valido.
            }
        }
    }

    private Order.StatusChange statusChange(OrderStatus from, OrderStatus to, Caller caller, String note) {
        return Order.StatusChange.builder().from(from).to(to).changedById(caller.userId()).changedByName(caller.name()).note(note).changedAt(Instant.now()).build();
    }

    public OrderService(final OrderRepository orderRepository, final SequenceService sequenceService, final CompatibilidadClient compatibilidadClient, final CatalogoClient catalogoClient) {
        this.orderRepository = orderRepository;
        this.sequenceService = sequenceService;
        this.compatibilidadClient = compatibilidadClient;
        this.catalogoClient = catalogoClient;
    }
}
