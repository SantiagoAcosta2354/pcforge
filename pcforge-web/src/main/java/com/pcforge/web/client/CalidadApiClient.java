package com.pcforge.web.client;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Acceso al area de calidad del dato tecnico del microservicio de catalogo.
 *
 * Es la parte del catalogo que pertenece al curador: atributos obligatorios,
 * fuente de cada dato y cobertura del catalogo. Los endpoints exigen token y
 * el servicio vuelve a comprobar el rol, de modo que esta capa no es la que
 * autoriza, solo la que muestra.
 */
@Component
public class CalidadApiClient {

    private final RestClient restClient;

    public CalidadApiClient(@Value("${pcforge.catalogo.base-url}") String baseUrl,
            @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        this.restClient = ApiSupport.build(baseUrl, timeoutMs);
    }

    public List<PendingSheet> pending(boolean includeUnverified, String authorization) {
        try {
            PendingSheet[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/catalog-quality/pending")
                            .queryParam("includeUnverified", includeUnverified).build())
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(PendingSheet[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public TechnicalSheet sheet(String productId, String authorization) {
        try {
            return restClient.get().uri("/api/v1/catalog-quality/products/{id}", productId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(TechnicalSheet.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public TechnicalSheet updateAttributes(String productId, Map<String, Object> body, String authorization) {
        try {
            return restClient.put().uri("/api/v1/catalog-quality/products/{id}/attributes", productId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .body(body)
                    .retrieve().body(TechnicalSheet.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public Coverage coverage(String authorization) {
        try {
            return restClient.get().uri("/api/v1/catalog-quality/coverage")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(Coverage.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    // ------------------------------------------------------------- vistas

    public record PendingSheet(
            String id,
            String sku,
            String name,
            String categoryId,
            String categoryName,
            String componentType,
            boolean active,
            List<String> missingAttributes,
            List<String> missingLabels,
            List<String> attributesWithoutSource,
            boolean blocksCompatibility) {
    }

    public record TechnicalSheet(
            String id,
            String sku,
            String name,
            String brand,
            String categoryId,
            String categoryName,
            String componentType,
            boolean active,
            Specs specs,
            Map<String, Source> sources,
            List<String> requiredAttributes,
            List<String> missingAttributes,
            List<String> attributesWithoutSource,
            String fichaStatus,
            boolean blocksCompatibility,
            Instant verifiedAt,
            String verifiedBy) {
    }

    public record Specs(
            String socket,
            List<String> ramTypeSupported,
            String ramType,
            Integer estimatedPowerW,
            Integer capacityW,
            String formFactor,
            Map<String, String> extra) {
    }

    public record Source(String source, Instant verifiedAt, String verifiedBy) {
    }

    public record Coverage(
            int totalProducts,
            int completeSheets,
            int incompleteSheets,
            int blockingSheets,
            double coveragePercentage,
            List<CategoryCoverage> byCategory,
            List<MissingAttribute> topMissingAttributes) {
    }

    public record CategoryCoverage(
            String categoryId,
            String categoryName,
            int totalProducts,
            int completeSheets,
            int incompleteSheets,
            double coveragePercentage) {
    }

    public record MissingAttribute(String attribute, String label, int productsAffected) {
    }
}
