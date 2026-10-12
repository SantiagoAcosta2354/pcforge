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
 * exigen un token valido con rol de administracion. La autorizacion se aplica
 * en el servidor: ocultar un boton en la pantalla no protege un endpoint.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // El consumo de componentes ocurre cuando un pedido entra a la linea de
        // ensamblaje, y esa transicion la ejecuta el asesor o el administrador.
        // Por eso los tres perfiles que participan en el proceso pueden registrar
        // movimientos de existencia. El mantenimiento del producto sigue siendo
        // exclusivo del administrador.
        http.csrf(csrf -> csrf.disable()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health/**", "/actuator/health", "/actuator/info").permitAll().requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll().requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/categories/**").permitAll().requestMatchers(HttpMethod.POST, "/api/v1/products/*/stock").hasAnyRole("ADMIN", "ASESOR", "TECNICO").requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN").requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN").requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasRole("ADMIN").requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN").requestMatchers("/api/v1/categories/**").hasRole("ADMIN").anyRequest().authenticated()).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)).authenticationEntryPoint((request, response, ex) -> writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Se requiere un token valido para realizar esta operacion")).accessDeniedHandler((request, response, ex) -> writeError(response, HttpServletResponse.SC_FORBIDDEN, "El usuario autenticado no tiene permisos para esta operacion")));
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
