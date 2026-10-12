package com.pcforge.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la capa web.
 *
 * El inicio de sesion lo resuelve PcforgeAuthenticationProvider contra el
 * microservicio de usuarios; aqui solo se declara que paginas son publicas y
 * que se hace con la sesion.
 *
 * La proteccion CSRF se mantiene activa: es una aplicacion con formularios y
 * sesion de servidor, justamente el escenario donde este ataque aplica. Cada
 * formulario incluye su campo oculto de forma explicita.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // El catalogo es publico: un visitante debe poder ver los productos
                // antes de decidir si se registra.
                .requestMatchers("/", "/catalogo", "/catalogo/**", "/login", "/registro",
                        "/css/**", "/js/**", "/img/**", "/favicon.ico",
                        "/actuator/health/**", "/actuator/health", "/actuator/info", "/error").permitAll()
                // El inventario solo lo gestiona el administrador.
                .requestMatchers("/inventario/**", "/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?sesionCerrada")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"))
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession()));
        return http.build();
    }
}
