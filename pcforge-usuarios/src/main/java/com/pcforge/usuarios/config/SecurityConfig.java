package com.pcforge.usuarios.config;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;

/**
 * El registro y el inicio de sesion son publicos; la administracion de cuentas
 * y roles queda restringida al administrador. Cada usuario puede consultar y
 * modificar solo sus propios datos a traves de las rutas /me.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // El asesor comercial necesita la lista de tecnicos para asignar
        // el ensamblaje de un pedido; la administracion de cuentas sigue
        // siendo exclusiva del administrador.
        http.csrf(csrf -> csrf.disable()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health/**", "/actuator/health", "/actuator/info").permitAll().requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll().requestMatchers("/api/v1/auth/**").permitAll().requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated().requestMatchers(HttpMethod.PATCH, "/api/v1/users/me/password").authenticated().requestMatchers(HttpMethod.GET, "/api/v1/users/technicians").hasAnyRole("ADMIN", "ASESOR").requestMatchers("/api/v1/users/**").hasRole("ADMIN").anyRequest().authenticated()).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)).authenticationEntryPoint((request, response, ex) -> writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Se requiere un token valido para realizar esta operacion")).accessDeniedHandler((request, response, ex) -> writeError(response, HttpServletResponse.SC_FORBIDDEN, "El usuario autenticado no tiene permisos para esta operacion")));
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
