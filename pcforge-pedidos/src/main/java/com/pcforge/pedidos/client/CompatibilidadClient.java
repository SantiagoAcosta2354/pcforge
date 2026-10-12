package com.pcforge.pedidos.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.pcforge.pedidos.exception.AccessDeniedForUserException;
import com.pcforge.pedidos.exception.DownstreamUnavailableException;
import com.pcforge.pedidos.exception.ResourceNotFoundException;

/**
 * Consulta la configuracion armada en el microservicio de compatibilidad.
 *
 * El token del usuario se reenvia tal cual. Esa decision es deliberada: la
 * configuracion tiene dueÃ±o, y si este servicio consultara con credenciales
 * propias podria leer la configuracion de cualquier cliente. Reenviando el
 * token, la verificacion de propiedad la sigue haciendo el servicio que es
 * dueÃ±o del dato.
 */
@Component
public class CompatibilidadClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompatibilidadClient.class);
    private final RestClient restClient;

    public CompatibilidadClient(@Value("${pcforge.compatibilidad.base-url}") String baseUrl, @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public CompatibilidadConfiguration findConfiguration(String configurationId, String bearerToken) {
        try {
            return restClient.get().uri("/api/v1/configurations/{id}", configurationId).header(HttpHeaders.AUTHORIZATION, bearerToken).retrieve().body(CompatibilidadConfiguration.class);
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            if (status == 404) {
                throw new ResourceNotFoundException("No existe la configuracion " + configurationId);
            }
            if (status == 403) {
                throw new AccessDeniedForUserException("La configuracion indicada pertenece a otro usuario");
            }
            log.error("Compatibilidad respondio {} al consultar la configuracion {}", status, configurationId);
            throw new DownstreamUnavailableException("El servicio de compatibilidad no pudo atender la consulta");
        } catch (Exception ex) {
            log.error("No fue posible comunicarse con el servicio de compatibilidad", ex);
            throw new DownstreamUnavailableException("El servicio de compatibilidad no esta disponible");
        }
    }

    /**
     * Vista parcial de la configuracion: solo lo que este servicio necesita.
     */
    public record CompatibilidadConfiguration(String id, String ownerId, String name, List<Item> items, BigDecimal estimatedTotal, String lastResult) {

        public record Item(String itemId, String productId, String sku, String name, String componentType, BigDecimal price, int quantity) {
        }
    }
}
