package com.pcforge.web.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Acceso al microservicio de pedidos: solicitudes, estados y ensamblaje.
 */
@Component
public class PedidosApiClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PedidosApiClient.class);
    private final RestClient restClient;

    public PedidosApiClient(@Value("${pcforge.pedidos.base-url}") String baseUrl, @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        this.restClient = ApiSupport.build(baseUrl, timeoutMs);
    }

    public OrderView create(String configurationId, String customerNote, String authorization) {
        try {
            return restClient.post().uri("/api/v1/orders").header(HttpHeaders.AUTHORIZATION, authorization).body(Map.of("configurationId", configurationId, "customerNote", customerNote == null ? "" : customerNote)).retrieve().body(OrderView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "pedidos");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("pedidos", ex);
        }
    }

    public List<OrderView> orders(String status, String authorization) {
        try {
            OrderView[] response = restClient.get().uri(uriBuilder -> {
                var builder = uriBuilder.path("/api/v1/orders");
                if (status != null && !status.isBlank()) {
                    builder.queryParam("status", status);
                }
                return builder.build();
            }).header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(OrderView[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "pedidos");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("pedidos", ex);
        }
    }

    public OrderView order(String idOrNumber, String authorization) {
        try {
            return restClient.get().uri("/api/v1/orders/{id}", idOrNumber).header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(OrderView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "pedidos");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("pedidos", ex);
        }
    }

    public OrderView changeStatus(String orderId, String status, String note, String technicianId, String technicianName, String authorization) {
        try {
            return restClient.patch().uri("/api/v1/orders/{id}/status", orderId).header(HttpHeaders.AUTHORIZATION, authorization).body(Map.of("status", status, "note", note == null ? "" : note, "technicianId", technicianId == null ? "" : technicianId, "technicianName", technicianName == null ? "" : technicianName)).retrieve().body(OrderView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "pedidos");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("pedidos", ex);
        }
    }

    public OrderView addObservation(String orderId, String text, String authorization) {
        try {
            return restClient.post().uri("/api/v1/orders/{id}/observations", orderId).header(HttpHeaders.AUTHORIZATION, authorization).body(Map.of("text", text)).retrieve().body(OrderView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "pedidos");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("pedidos", ex);
        }
    }

    // ------------------------------------------------------------- vistas
    public record OrderView(String id, String orderNumber, String customerId, String customerEmail, String customerName, String configurationId, String compatibilityResult, List<ItemView> items, BigDecimal subtotal, BigDecimal total, String customerNote, String status, List<String> allowedNextStatuses, String assignedTechnicianId, String assignedTechnicianName, List<ObservationView> observations, List<StatusChangeView> history, boolean stockDiscounted, Instant createdAt, Instant updatedAt, Instant deliveredAt) {
    }

    public record ItemView(String productId, String sku, String name, String componentType, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }

    public record ObservationView(String authorId, String authorName, String authorRole, String text, Instant createdAt) {
    }

    public record StatusChangeView(String from, String to, String changedByName, String note, Instant changedAt) {
    }
}
