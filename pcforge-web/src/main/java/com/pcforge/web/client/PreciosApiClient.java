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
 * Acceso a la politica comercial y al margen de cada referencia.
 *
 * El costo viaja en estas respuestas y no en la consulta publica del catalogo:
 * es informacion de margen, no de escaparate. El cliente envia el token del
 * usuario y el servicio comprueba que sea analista o administrador.
 */
@Component
public class PreciosApiClient {

    private final RestClient restClient;

    public PreciosApiClient(@Value("${pcforge.catalogo.base-url}") String baseUrl,
            @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        this.restClient = ApiSupport.build(baseUrl, timeoutMs);
    }

    public List<Policy> policies(String authorization) {
        try {
            Policy[] response = restClient.get().uri("/api/v1/pricing/policy")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(Policy[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public Policy updatePolicy(String categoryId, BigDecimal minimumMargin, BigDecimal discountThreshold,
            String authorization) {
        try {
            return restClient.put().uri("/api/v1/pricing/policy/{id}", categoryId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .body(Map.of("minimumMargin", minimumMargin, "discountThreshold", discountThreshold))
                    .retrieve().body(Policy.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public List<Margin> margins(boolean onlyAlerts, String authorization) {
        try {
            Margin[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/pricing/margins")
                            .queryParam("onlyAlerts", onlyAlerts).build())
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(Margin[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    // ------------------------------------------------------------- vistas

    public record Policy(
            String categoryId,
            String categoryName,
            String slug,
            BigDecimal minimumMargin,
            BigDecimal discountThreshold,
            String policyUpdatedBy,
            Instant policyUpdatedAt,
            String policySource) {
    }

    public record Margin(
            String productId,
            String sku,
            String name,
            String categoryId,
            String categoryName,
            boolean active,
            BigDecimal cost,
            BigDecimal price,
            BigDecimal marginPercentage,
            BigDecimal minimumMargin,
            BigDecimal gap,
            String status,
            String policySource) {
    }
}
