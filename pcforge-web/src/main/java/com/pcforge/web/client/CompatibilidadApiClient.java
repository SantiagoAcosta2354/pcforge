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
 * Acceso al microservicio de compatibilidad: configuraciones y validaciones.
 */
@Component
public class CompatibilidadApiClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompatibilidadApiClient.class);
    private final RestClient restClient;

    public CompatibilidadApiClient(@Value("${pcforge.compatibilidad.base-url}") String baseUrl, @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        this.restClient = ApiSupport.build(baseUrl, timeoutMs);
    }

    public ConfigurationView create(String name, String authorization) {
        try {
            return restClient.post().uri("/api/v1/configurations").header(HttpHeaders.AUTHORIZATION, authorization).body(Map.of("name", name == null ? "Configuracion" : name)).retrieve().body(ConfigurationView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "compatibilidad");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("compatibilidad", ex);
        }
    }

    public ConfigurationView configuration(String configurationId, String authorization) {
        try {
            return restClient.get().uri("/api/v1/configurations/{id}", configurationId).header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(ConfigurationView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "compatibilidad");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("compatibilidad", ex);
        }
    }

    public ConfigurationView addItem(String configurationId, String productId, int quantity, String authorization) {
        try {
            return restClient.post().uri("/api/v1/configurations/{id}/items", configurationId).header(HttpHeaders.AUTHORIZATION, authorization).body(Map.of("productId", productId, "quantity", quantity)).retrieve().body(ConfigurationView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "compatibilidad");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("compatibilidad", ex);
        }
    }

    public ConfigurationView removeItem(String configurationId, String itemId, String authorization) {
        try {
            return restClient.delete().uri("/api/v1/configurations/{id}/items/{itemId}", configurationId, itemId).header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(ConfigurationView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "compatibilidad");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("compatibilidad", ex);
        }
    }

    public ValidationView validate(String configurationId, String authorization) {
        try {
            return restClient.post().uri("/api/v1/configurations/{id}/validate", configurationId).header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(ValidationView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "compatibilidad");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("compatibilidad", ex);
        }
    }

    // ------------------------------------------------------------- vistas
    public record ConfigurationView(String id, String ownerId, String name, List<ItemView> items, BigDecimal estimatedTotal, String lastResult, Instant createdAt, Instant updatedAt) {
    }

    public record ItemView(String itemId, String productId, String sku, String name, String componentType, BigDecimal price, int quantity, SpecsView specs) {
    }

    public record SpecsView(String socket, List<String> ramTypeSupported, String ramType, Integer estimatedPowerW, Integer capacityW, String formFactor) {
    }

    public record ValidationView(String id, String configurationId, String result, List<CheckView> checks, List<String> missingData, Instant createdAt) {
    }

    public record CheckView(String rule, String status, String message) {
    }
}
