package com.pcforge.pedidos.config;

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
    OpenAPI pedidosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PCForge - Cotizaciones, pedidos y ensamblaje")
                        .version("v1")
                        .description("Registro de solicitudes a partir de una configuracion armada, seguimiento "
                                + "del proceso comercial y de las etapas del servicio de ensamblaje.")
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
