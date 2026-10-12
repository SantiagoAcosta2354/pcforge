package com.pcforge.pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.pcforge.pedidos.client.CatalogoClient;
import com.pcforge.pedidos.client.CompatibilidadClient;
import com.pcforge.pedidos.domain.Order;
import com.pcforge.pedidos.domain.OrderStatus;
import com.pcforge.pedidos.exception.AccessDeniedForUserException;
import com.pcforge.pedidos.exception.BusinessRuleException;
import com.pcforge.pedidos.exception.DownstreamUnavailableException;
import com.pcforge.pedidos.repository.OrderRepository;
import com.pcforge.pedidos.web.dto.AddObservationRequest;
import com.pcforge.pedidos.web.dto.ChangeStatusRequest;
import com.pcforge.pedidos.web.dto.CreateOrderRequest;
import com.pcforge.pedidos.web.dto.OrderResponse;

/**
 * Pruebas de las reglas de pedidos. Los servicios remotos se simulan: estas
 * pruebas verifican las decisiones propias de este servicio (disponibilidad,
 * transiciones, permisos e inventario) y no la integracion, que se comprueba
 * aparte con los servicios en ejecucion.
 */
class OrderServiceTest {

    private OrderRepository orderRepository;
    private SequenceService sequenceService;
    private CompatibilidadClient compatibilidadClient;
    private CatalogoClient catalogoClient;
    private OrderService orderService;

    private final Caller cliente = new Caller("u-cli", "cliente@pcforge.co", "Cliente", List.of("CLIENTE"), "Bearer t");
    private final Caller asesor = new Caller("u-ase", "asesor@pcforge.co", "Asesor", List.of("ASESOR"), "Bearer t");
    private final Caller tecnico = new Caller("u-tec", "tecnico@pcforge.co", "Tecnico", List.of("TECNICO"), "Bearer t");
    private final Caller otroCliente = new Caller("u-otro", "otro@pcforge.co", "Otro", List.of("CLIENTE"), "Bearer t");

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        sequenceService = Mockito.mock(SequenceService.class);
        compatibilidadClient = Mockito.mock(CompatibilidadClient.class);
        catalogoClient = Mockito.mock(CatalogoClient.class);
        orderService = new OrderService(orderRepository, sequenceService, compatibilidadClient, catalogoClient);

        when(sequenceService.nextOrderNumber()).thenReturn("PF-000001");
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ------------------------------------------------------------- registro

    @Test
    @DisplayName("La solicitud se registra con los precios vigentes del catalogo")
    void shouldCreateOrderWithCatalogPrices() {
        when(compatibilidadClient.findConfiguration(anyString(), anyString())).thenReturn(configurationConCpu());
        when(catalogoClient.findProduct("p-cpu")).thenReturn(product("p-cpu", "CPU-1", new BigDecimal("700000"), 5));

        OrderResponse response = orderService.create(cliente, new CreateOrderRequest("cfg-1", "Para jugar"));

        assertThat(response.orderNumber()).isEqualTo("PF-000001");
        assertThat(response.status()).isEqualTo(OrderStatus.RECIBIDO);
        assertThat(response.total()).isEqualByComparingTo("700000");
        assertThat(response.items()).hasSize(1);
        assertThat(response.history()).hasSize(1);
        assertThat(response.stockDiscounted()).isFalse();
    }

    @Test
    @DisplayName("No se registra la solicitud si no hay existencias suficientes")
    void shouldRejectWhenStockIsInsufficient() {
        when(compatibilidadClient.findConfiguration(anyString(), anyString())).thenReturn(configurationConCpu());
        when(catalogoClient.findProduct("p-cpu")).thenReturn(product("p-cpu", "CPU-1", new BigDecimal("700000"), 0));

        assertThatThrownBy(() -> orderService.create(cliente, new CreateOrderRequest("cfg-1", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CPU-1")
                .hasMessageContaining("disponible 0");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("No se registra una solicitud a partir de una configuracion vacia")
    void shouldRejectEmptyConfiguration() {
        when(compatibilidadClient.findConfiguration(anyString(), anyString()))
                .thenReturn(new CompatibilidadClient.CompatibilidadConfiguration(
                        "cfg-1", "u-cli", "Vacia", List.of(), BigDecimal.ZERO, null));

        assertThatThrownBy(() -> orderService.create(cliente, new CreateOrderRequest("cfg-1", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no tiene componentes");
    }

    // ---------------------------------------------------------- transiciones

    @Test
    @DisplayName("No se permite un salto de estado no contemplado")
    void shouldRejectInvalidTransition() {
        Order order = order(OrderStatus.RECIBIDO);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.changeStatus("o-1",
                new ChangeStatusRequest(OrderStatus.ENTREGADO, null, null, null), asesor))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede pasar de RECIBIDO a ENTREGADO");
    }

    @Test
    @DisplayName("El cliente no puede tomar su propia solicitud para revision")
    void shouldRejectCustomerMovingToReview() {
        Order order = order(OrderStatus.RECIBIDO);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.changeStatus("o-1",
                new ChangeStatusRequest(OrderStatus.EN_REVISION, null, null, null), cliente))
                .isInstanceOf(AccessDeniedForUserException.class);
    }

    @Test
    @DisplayName("El tecnico no puede confirmar la cotizacion")
    void shouldRejectTechnicianConfirmingQuote() {
        Order order = order(OrderStatus.EN_REVISION);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.changeStatus("o-1",
                new ChangeStatusRequest(OrderStatus.COTIZADO, null, null, null), tecnico))
                .isInstanceOf(AccessDeniedForUserException.class);
    }

    @Test
    @DisplayName("El cliente puede anular su solicitud mientras esta en revision")
    void shouldAllowCustomerToCancelEarlyOrder() {
        Order order = order(OrderStatus.EN_REVISION);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.changeStatus("o-1",
                new ChangeStatusRequest(OrderStatus.CANCELADO, "Ya no lo necesito", null, null), cliente);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELADO);
        assertThat(response.history()).hasSize(1);
    }

    // ------------------------------------------------------------- inventario

    @Test
    @DisplayName("Al entrar a ensamblaje se descuentan las existencias del catalogo")
    void shouldDiscountStockWhenAssemblyStarts() {
        Order order = order(OrderStatus.COTIZADO);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));
        when(catalogoClient.registerExit(anyString(), anyInt(), anyString(), anyString())).thenReturn(3);

        OrderResponse response = orderService.changeStatus("o-1",
                new ChangeStatusRequest(OrderStatus.EN_ENSAMBLAJE, "Cliente acepto", null, null), asesor);

        assertThat(response.status()).isEqualTo(OrderStatus.EN_ENSAMBLAJE);
        assertThat(response.stockDiscounted()).isTrue();
        verify(catalogoClient).registerExit("p-cpu", 1, "Pedido PF-000001", "Bearer t");
    }

    @Test
    @DisplayName("Si el inventario no se puede actualizar, el pedido no avanza de estado")
    void shouldNotAdvanceWhenStockCannotBeDiscounted() {
        Order order = order(OrderStatus.COTIZADO);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));
        when(catalogoClient.registerExit(anyString(), anyInt(), anyString(), anyString()))
                .thenThrow(new DownstreamUnavailableException("El catalogo no esta disponible"));

        assertThatThrownBy(() -> orderService.changeStatus("o-1",
                new ChangeStatusRequest(OrderStatus.EN_ENSAMBLAJE, null, null, null), asesor))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inventario no pudo actualizarse");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COTIZADO);
        assertThat(order.isStockDiscounted()).isFalse();
        verify(orderRepository, never()).save(any(Order.class));
    }

    // --------------------------------------------------------------- acceso

    @Test
    @DisplayName("Un cliente no puede consultar el pedido de otro")
    void shouldDenyAccessToAnotherCustomersOrder() {
        Order order = order(OrderStatus.RECIBIDO);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.find("o-1", otroCliente))
                .isInstanceOf(AccessDeniedForUserException.class);
    }

    @Test
    @DisplayName("El tecnico si puede consultar el pedido de un cliente")
    void shouldAllowTechnicianToSeeAnyOrder() {
        Order order = order(OrderStatus.EN_ENSAMBLAJE);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));

        assertThat(orderService.find("o-1", tecnico).orderNumber()).isEqualTo("PF-000001");
    }

    @Test
    @DisplayName("Un pedido ya entregado no admite mas observaciones")
    void shouldRejectObservationsOnClosedOrder() {
        Order order = order(OrderStatus.ENTREGADO);
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.addObservation("o-1", new AddObservationRequest("Nota"), tecnico))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no admite observaciones");
    }

    // ----------------------------------------------------------------- apoyo

    private Order order(OrderStatus status) {
        Order order = Order.builder()
                .id("o-1")
                .orderNumber("PF-000001")
                .customerId("u-cli")
                .customerEmail("cliente@pcforge.co")
                .customerName("Cliente")
                .configurationId("cfg-1")
                .status(status)
                .subtotal(new BigDecimal("700000"))
                .total(new BigDecimal("700000"))
                .items(new java.util.ArrayList<>(List.of(Order.OrderItem.builder()
                        .productId("p-cpu").sku("CPU-1").name("Procesador")
                        .componentType("PROCESADOR").unitPrice(new BigDecimal("700000"))
                        .quantity(1).lineTotal(new BigDecimal("700000")).build())))
                .build();
        return order;
    }

    private CompatibilidadClient.CompatibilidadConfiguration configurationConCpu() {
        return new CompatibilidadClient.CompatibilidadConfiguration(
                "cfg-1", "u-cli", "Equipo", List.of(
                        new CompatibilidadClient.CompatibilidadConfiguration.Item(
                                "i-1", "p-cpu", "CPU-1", "Procesador", "PROCESADOR",
                                new BigDecimal("700000"), 1)),
                new BigDecimal("700000"), "OK");
    }

    private CatalogoClient.CatalogoProduct product(String id, String sku, BigDecimal price, int stock) {
        return new CatalogoClient.CatalogoProduct(id, sku, "Procesador", "PROCESADOR", price, stock, true, stock > 0);
    }
}
