package com.pcforge.pedidos.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.pcforge.pedidos.domain.Order;
import com.pcforge.pedidos.domain.OrderStatus;

/**
 * Representacion de un pedido. Incluye los estados a los que puede pasar
 * segun el estado actual, para que la interfaz no tenga que repetir las
 * reglas de transicion.
 */
public record OrderResponse(
        String id,
        String orderNumber,
        String customerId,
        String customerEmail,
        String customerName,
        String configurationId,
        String compatibilityResult,
        List<Item> items,
        BigDecimal subtotal,
        BigDecimal total,
        String customerNote,
        OrderStatus status,
        List<OrderStatus> allowedNextStatuses,
        String assignedTechnicianId,
        String assignedTechnicianName,
        List<Observation> observations,
        List<StatusChange> history,
        boolean stockDiscounted,
        Instant createdAt,
        Instant updatedAt,
        Instant deliveredAt) {

    public record Item(String productId, String sku, String name, String componentType,
                       BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {

        static Item from(Order.OrderItem i) {
            return new Item(i.getProductId(), i.getSku(), i.getName(), i.getComponentType(),
                    i.getUnitPrice(), i.getQuantity(), i.getLineTotal());
        }
    }

    public record Observation(String authorId, String authorName, String authorRole,
                              String text, Instant createdAt) {

        static Observation from(Order.Observation o) {
            return new Observation(o.getAuthorId(), o.getAuthorName(), o.getAuthorRole(),
                    o.getText(), o.getCreatedAt());
        }
    }

    public record StatusChange(OrderStatus from, OrderStatus to, String changedByName,
                               String note, Instant changedAt) {

        static StatusChange from(Order.StatusChange c) {
            return new StatusChange(c.getFrom(), c.getTo(), c.getChangedByName(), c.getNote(), c.getChangedAt());
        }
    }

    public static OrderResponse from(Order o, List<OrderStatus> allowedNext) {
        return new OrderResponse(
                o.getId(), o.getOrderNumber(), o.getCustomerId(), o.getCustomerEmail(), o.getCustomerName(),
                o.getConfigurationId(), o.getCompatibilityResult(),
                o.getItems().stream().map(Item::from).toList(),
                o.getSubtotal(), o.getTotal(), o.getCustomerNote(), o.getStatus(), allowedNext,
                o.getAssignedTechnicianId(), o.getAssignedTechnicianName(),
                o.getObservations().stream().map(Observation::from).toList(),
                o.getHistory().stream().map(StatusChange::from).toList(),
                o.isStockDiscounted(), o.getCreatedAt(), o.getUpdatedAt(), o.getDeliveredAt());
    }
}
