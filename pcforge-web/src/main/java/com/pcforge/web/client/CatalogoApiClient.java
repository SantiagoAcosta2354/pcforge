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
 * Acceso al microservicio de catalogo: productos, categorias e inventario.
 */
@Component
public class CatalogoApiClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CatalogoApiClient.class);
    private final RestClient restClient;

    public CatalogoApiClient(@Value("${pcforge.catalogo.base-url}") String baseUrl, @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        this.restClient = ApiSupport.build(baseUrl, timeoutMs);
    }

    public List<ProductView> products(String categoryId, String componentType, String name) {
        try {
            ProductView[] response = restClient.get().uri(uriBuilder -> {
                var builder = uriBuilder.path("/api/v1/products");
                if (categoryId != null && !categoryId.isBlank()) {
                    builder.queryParam("categoryId", categoryId);
                }
                if (componentType != null && !componentType.isBlank()) {
                    builder.queryParam("componentType", componentType);
                }
                if (name != null && !name.isBlank()) {
                    builder.queryParam("name", name);
                }
                return builder.build();
            }).retrieve().body(ProductView[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public ProductView product(String productId) {
        try {
            return restClient.get().uri("/api/v1/products/{id}", productId).retrieve().body(ProductView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public List<ProductView> lowStock() {
        try {
            ProductView[] response = restClient.get().uri("/api/v1/products/low-stock").retrieve().body(ProductView[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public List<CategoryView> categories() {
        try {
            CategoryView[] response = restClient.get().uri("/api/v1/categories").retrieve().body(CategoryView[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public ProductView adjustStock(String productId, String movementType, int quantity, String reason, String authorization) {
        try {
            return restClient.post().uri("/api/v1/products/{id}/stock", productId).header(HttpHeaders.AUTHORIZATION, authorization).body(Map.of("movementType", movementType, "quantity", quantity, "reason", reason)).retrieve().body(ProductView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    /**
     * Listado del administrador: a diferencia del publico, incluye los productos
     * desactivados, que son justamente los que necesita ver para reactivarlos.
     */
    public List<ProductView> adminProducts(String categoryId, String componentType, String name) {
        try {
            ProductView[] response = restClient.get().uri(uriBuilder -> {
                var builder = uriBuilder.path("/api/v1/products").queryParam("includeInactive", true);
                if (categoryId != null && !categoryId.isBlank()) {
                    builder.queryParam("categoryId", categoryId);
                }
                if (componentType != null && !componentType.isBlank()) {
                    builder.queryParam("componentType", componentType);
                }
                if (name != null && !name.isBlank()) {
                    builder.queryParam("name", name);
                }
                return builder.build();
            }).retrieve().body(ProductView[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    /** Categorias incluyendo las desactivadas, para la pantalla de gestion. */
    public List<CategoryView> adminCategories() {
        try {
            CategoryView[] response = restClient.get().uri(uriBuilder -> uriBuilder.path("/api/v1/categories").queryParam("includeInactive", true).build()).retrieve().body(CategoryView[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public ProductView createProduct(Map<String, Object> body, String authorization) {
        try {
            return restClient.post().uri("/api/v1/products").header(HttpHeaders.AUTHORIZATION, authorization).body(body).retrieve().body(ProductView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public ProductView updateProduct(String productId, Map<String, Object> body, String authorization) {
        try {
            return restClient.put().uri("/api/v1/products/{id}", productId).header(HttpHeaders.AUTHORIZATION, authorization).body(body).retrieve().body(ProductView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public ProductView setProductStatus(String productId, boolean active, String authorization) {
        try {
            return restClient.patch().uri(uriBuilder -> uriBuilder.path("/api/v1/products/{id}/status").queryParam("active", active).build(productId)).header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(ProductView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public CategoryView createCategory(Map<String, Object> body, String authorization) {
        try {
            return restClient.post().uri("/api/v1/categories").header(HttpHeaders.AUTHORIZATION, authorization).body(body).retrieve().body(CategoryView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public CategoryView updateCategory(String categoryId, Map<String, Object> body, String authorization) {
        try {
            return restClient.put().uri("/api/v1/categories/{id}", categoryId).header(HttpHeaders.AUTHORIZATION, authorization).body(body).retrieve().body(CategoryView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    public CategoryView setCategoryStatus(String categoryId, boolean active, String authorization) {
        try {
            return restClient.patch().uri(uriBuilder -> uriBuilder.path("/api/v1/categories/{id}/status").queryParam("active", active).build(categoryId)).header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(CategoryView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "catalogo");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("catalogo", ex);
        }
    }

    // ------------------------------------------------------------- vistas
    public record ProductView(String id, String sku, String name, String brand, String categoryId, String componentType, String description, BigDecimal price, int stock, int minimumStock, boolean active, boolean available, boolean lowStock, SpecsView specs, Instant createdAt, Instant updatedAt) {
    }

    public record SpecsView(String socket, List<String> ramTypeSupported, String ramType, Integer estimatedPowerW, Integer capacityW, String formFactor, Map<String, String> extra) {
    }

    public record CategoryView(String id, String name, String slug, String description, boolean active) {
    }
}
