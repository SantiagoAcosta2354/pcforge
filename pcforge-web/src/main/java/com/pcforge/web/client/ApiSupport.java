package com.pcforge.web.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.pcforge.web.exception.BusinessMessageException;
import com.pcforge.web.exception.DownstreamUnavailableException;
import com.pcforge.web.exception.NotFoundInViewException;

/**
 * Apoyo comun a los clientes de la capa web.
 *
 * Los servicios responden a los errores con un mensaje ya redactado en
 * lenguaje claro ({status, error, message}). Aqui se extrae ese mensaje para
 * mostrarlo en pantalla, en lugar de inventar uno nuevo o de mostrar el codigo
 * HTTP. Se lee con una expresion regular a proposito, para no depender de la
 * version del serializador JSON.
 */
final class ApiSupport {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiSupport.class);
    private static final Pattern MESSAGE_FIELD = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern DETAILS_FIELD = Pattern.compile("\"details\"\\s*:\\s*\\[(.*?)]");
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]*)\"");

    private ApiSupport() {
    }

    /**
     * Construye el cliente HTTP.
     *
     * Se usa el cliente del JDK y no SimpleClientHttpRequestFactory porque este
     * ultimo se apoya en HttpURLConnection, que no admite el metodo PATCH. Los
     * cambios de estado de un pedido se hacen con PATCH, asi que con aquel
     * fabrica las peticiones fallarian con "Invalid HTTP method: PATCH".
     */
    static RestClient build(String baseUrl, int timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    static RuntimeException translate(RestClientResponseException ex, String serviceLabel) {
        int status = ex.getStatusCode().value();
        String message = extractMessage(ex.getResponseBodyAsString());
        if (status >= 500) {
            return new DownstreamUnavailableException("El servicio de " + serviceLabel + " no pudo atender la solicitud en este momento.");
        }
        if (status == 404) {
            return new NotFoundInViewException(message != null ? message : "El recurso solicitado no existe en el servicio de " + serviceLabel + ".");
        }
        if (status == 401 || status == 403) {
            return new BusinessMessageException(message != null ? message : "No tiene permisos para realizar esa operacion.");
        }
        if (message == null) {
            return new BusinessMessageException("La operacion no pudo completarse en el servicio de " + serviceLabel + ".");
        }
        // El servicio explica que campo fallo; ese detalle es lo que necesita
        // ver quien esta usando la aplicacion.
        String details = extractDetails(ex.getResponseBodyAsString());
        return new BusinessMessageException(details == null ? message : message + " (" + details + ")");
    }

    private static String extractDetails(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        Matcher match = DETAILS_FIELD.matcher(body);
        if (!match.find()) {
            return null;
        }
        Matcher quoted = QUOTED.matcher(match.group(1));
        List<String> details = new ArrayList<>();
        while (quoted.find()) {
            details.add(quoted.group(1));
        }
        return details.isEmpty() ? null : String.join("; ", details);
    }

    static RuntimeException unavailable(String serviceLabel, Exception cause) {
        // El motivo real (mÃ©todo no soportado, puerto cerrado, tiempo agotado)
        // queda en el registro: en pantalla se muestra un mensaje amable.
        log.error("Fallo la comunicacion con el servicio de {}: {}", serviceLabel, cause.toString());
        return new DownstreamUnavailableException("No fue posible comunicarse con el servicio de " + serviceLabel + ". Si acaba de desplegarse, el servicio puede tardar unos segundos en responder.", cause);
    }

    private static String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        Matcher matcher = MESSAGE_FIELD.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }
}
