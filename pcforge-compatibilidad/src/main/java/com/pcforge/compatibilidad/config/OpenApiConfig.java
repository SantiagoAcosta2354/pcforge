package com.pcforge.compatibilidad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI compatibilidadOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PCForge - Configurador y compatibilidad")
                        .version("v1")
                        .description("Armado de configuraciones de equipos y validacion de reglas basicas de "
                                + "compatibilidad: socket, tipo de memoria y potencia. Cuando faltan datos "
                                + "tecnicos el resultado es NO_CONCLUYENTE.")
                        .contact(new Contact().name("Equipo PCForge")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token emitido por el microservicio de usuarios")));
    }
}
