package com.pcforge.compatibilidad.client;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.pcforge.compatibilidad.exception.CatalogUnavailableException;
import com.pcforge.compatibilidad.exception.ResourceNotFoundException;

/**
 * Acceso al microservicio de catalogo.
 * Este servicio no consulta la base del catalogo: pide los datos por API,
 * que es lo que exige el patron database-per-service. Si el catalogo no
 * responde, se lanza un error: nunca se asume una compatibilidad.
 */
@Component
public class CatalogoClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CatalogoClient.class);
    private final RestClient restClient;

    public CatalogoClient(@Value("${pcforge.catalogo.base-url}") String baseUrl, @Value("${pcforge.catalogo.timeout-ms}") int timeoutMs) {
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
                throw new ResourceNotFoundException("El producto " + productId + " no existe en el catalogo");
            }
            log.error("El catalogo respondio {} al consultar el producto {}", ex.getStatusCode(), productId);
            throw new CatalogUnavailableException("El catalogo no pudo atender la consulta del producto");
        } catch (Exception ex) {
            log.error("No fue posible comunicarse con el catalogo", ex);
            throw new CatalogUnavailableException("El catalogo no esta disponible en este momento");
        }
    }
}
