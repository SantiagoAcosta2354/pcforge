package com.pcforge.catalogo.config;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * La consulta del catalogo es publica, porque un visitante debe poder ver los
 * productos antes de registrarse. Las operaciones que modifican informacion
 * exigen un token valido con el rol correspondiente. La autorizacion se aplica
 * en el servidor: ocultar un boton en la pantalla no protege un endpoint.
 *
 * Los seis roles del dominio llegan en el claim "roles" del token:
 * ADMIN, ASESOR, TECNICO, CURADOR y ANALISTA. CLIENTE no requiere un matcher
 * propio: solo consume las rutas publicas y las de pedidos, que viven en otro
 * microservicio.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --- publico: salud, documentacion y consulta del catalogo ---
                .requestMatchers("/actuator/health/**", "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/categories/**").permitAll()

                // --- existencias: el consumo ocurre en la linea de ensamblaje ---
                // Por eso los tres perfiles que participan en el proceso registran
                // movimientos. El mantenimiento del producto sigue siendo del administrador.
                .requestMatchers(HttpMethod.POST, "/api/v1/products/*/stock")
                    .hasAnyRole("ADMIN", "ASESOR", "TECNICO")

                // --- curador de datos tecnicos: solo atributos y calidad del catalogo ---
                // No tiene acceso a precios ni a costo, y su escritura no toca el producto.
                .requestMatchers("/api/v1/catalog-quality/**").hasAnyRole("CURADOR", "ADMIN")

                // --- analista de precios y margenes: politica y consulta de margen ---
                // El costo lo registra el administrador, no el analista, para que quien
                // define la politica no sea quien captura el dato que la alimenta.
                .requestMatchers(HttpMethod.PUT, "/api/v1/pricing/products/*/cost").hasRole("ADMIN")
                .requestMatchers("/api/v1/pricing/**").hasAnyRole("ANALISTA", "ADMIN")

                // --- mantenimiento del catalogo: administrador ---
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/categories/**").hasRole("ADMIN")

                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint((request, response, ex) -> writeError(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Se requiere un token valido para realizar esta operacion"))
                .accessDeniedHandler((request, response, ex) -> writeError(response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "El usuario autenticado no tiene permisos para esta operacion")));
        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + message + "\"}");
    }

    public SecurityConfig(final JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }
}
