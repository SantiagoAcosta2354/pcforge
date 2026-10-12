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
 * Acceso al microservicio de usuarios: autenticacion y cuentas.
 */
@Component
public class UsuariosApiClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UsuariosApiClient.class);
    private final RestClient restClient;

    public UsuariosApiClient(@Value("${pcforge.usuarios.base-url}") String baseUrl, @Value("${pcforge.downstream.timeout-ms}") int timeoutMs) {
        this.restClient = ApiSupport.build(baseUrl, timeoutMs);
    }

    public TokenView login(String email, String password) {
        try {
            return restClient.post().uri("/api/v1/auth/login").body(Map.of("email", email, "password", password)).retrieve().body(TokenView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "usuarios");
        } catch (Exception ex) {
            log.error("No fue posible autenticar contra el servicio de usuarios", ex);
            throw ApiSupport.unavailable("usuarios", ex);
        }
    }

    public UserView register(String fullName, String email, String password) {
        try {
            return restClient.post().uri("/api/v1/auth/register").body(Map.of("fullName", fullName, "email", email, "password", password)).retrieve().body(UserView.class);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "usuarios");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("usuarios", ex);
        }
    }

    /**
     * Tecnicos disponibles para asignarle un ensamblaje.
     */
    public List<UserView> technicians(String authorization) {
        try {
            UserView[] response = restClient.get().uri("/api/v1/users/technicians").header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(UserView[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientResponseException ex) {
            throw ApiSupport.translate(ex, "usuarios");
        } catch (Exception ex) {
            throw ApiSupport.unavailable("usuarios", ex);
        }
    }

    // ------------------------------------------------------------- vistas
    public record TokenView(String accessToken, String tokenType, long expiresInSeconds, UserView user) {
    }

    public record UserView(String id, String fullName, String email, List<String> roles, boolean active, Instant lastLoginAt, Instant createdAt) {
    }
}
