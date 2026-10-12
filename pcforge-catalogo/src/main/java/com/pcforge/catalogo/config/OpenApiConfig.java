package com.pcforge.catalogo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Documentacion de la API con el estandar OpenAPI, publicada por Swagger UI en
 * /swagger-ui.html. El esquema de seguridad permite probar los endpoints
 * protegidos pegando el token emitido por el servicio de usuarios.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI catalogoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PCForge - Catalogo e inventario")
                        .version("v1")
                        .description("Productos, categorias, atributos tecnicos y control de existencias "
                                + "de la plataforma PCForge.")
                        .contact(new Contact().name("Equipo PCForge")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT emitido por el microservicio de usuarios")));
    }
}
