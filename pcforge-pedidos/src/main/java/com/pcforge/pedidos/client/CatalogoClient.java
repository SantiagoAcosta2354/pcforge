package com.pcforge.pedidos.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.pcforge.pedidos.exception.DownstreamUnavailableException;
import com.pcforge.pedidos.exception.ResourceNotFoundException;

/**
 * Acceso al microservicio de catalogo. Se usa para dos cosas distintas:
 * volver a comprobar la disponibilidad al registrar una solicitud y descontar
 * existencias cuando el pedido entra en la linea de ensamblaje.
 */
@Component
public class CatalogoClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CatalogoClient.class);
    private final RestClient restClient;

    public CatalogoClient(@Value("${pcforge.catalogo.base-url}") String baseUrl, @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public CatalogoProduct findProduct(String productId) {
        try {
            return restClient.get().uri("/api/v1/products/{id}", productId).retrieve().body(CatalogoProduct.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("El producto " + productId + " ya no existe en el catalogo");
            }
            log.error("El catalogo respondio {} al consultar el producto {}", ex.getStatusCode(), productId);
            throw new DownstreamUnavailableException("El catalogo no pudo atender la consulta del producto");
        } catch (Exception ex) {
            log.error("No fue posible comunicarse con el catalogo", ex);
            throw new DownstreamUnavailableException("El catalogo no esta disponible en este momento");
        }
    }

    /**
     * Registra una salida de inventario. Devuelve la existencia resultante.
     */
    public int registerExit(String productId, int quantity, String reason, String bearerToken) {
        return registerMovement(productId, "SALIDA", quantity, reason, bearerToken);
    }

    /**
     * Devuelve existencias al catalogo (se usa para revertir un descuento fallido).
     */
    public int registerEntry(String productId, int quantity, String reason, String bearerToken) {
        return registerMovement(productId, "ENTRADA", quantity, reason, bearerToken);
    }

    private int registerMovement(String productId, String movementType, int quantity, String reason, String bearerToken) {
        try {
            CatalogoProduct updated = restClient.post().uri("/api/v1/products/{id}/stock", productId).header(HttpHeaders.AUTHORIZATION, bearerToken).body(Map.of("movementType", movementType, "quantity", quantity, "reason", reason)).retrieve().body(CatalogoProduct.class);
            return updated == null ? 0 : updated.stock();
        } catch (RestClientResponseException ex) {
            log.error("El catalogo respondio {} al registrar el movimiento {} del producto {}", ex.getStatusCode(), movementType, productId);
            throw new DownstreamUnavailableException("No fue posible actualizar las existencias del producto " + productId + " (respuesta " + ex.getStatusCode().value() + ")");
        } catch (Exception ex) {
            log.error("No fue posible actualizar existencias en el catalogo", ex);
            throw new DownstreamUnavailableException("El catalogo no esta disponible para actualizar existencias");
        }
    }

    /**
     * Vista parcial del producto del catalogo.
     */
    public record CatalogoProduct(String id, String sku, String name, String componentType, BigDecimal price, int stock, boolean active, boolean available) {
    }
}
