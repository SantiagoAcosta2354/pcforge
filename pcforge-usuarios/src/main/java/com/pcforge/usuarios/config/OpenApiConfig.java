package com.pcforge.usuarios.config;

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
    OpenAPI usuariosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PCForge - Usuarios y autenticacion")
                        .version("v1")
                        .description("Registro, inicio de sesion y administracion de cuentas y roles. "
                                + "Este servicio emite el token JWT que validan los demas microservicios.")
                        .contact(new Contact().name("Equipo PCForge")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token obtenido en /api/v1/auth/login")));
    }
}
