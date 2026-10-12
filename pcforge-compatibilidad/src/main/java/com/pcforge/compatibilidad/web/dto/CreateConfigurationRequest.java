package com.pcforge.compatibilidad.web.dto;

import jakarta.validation.constraints.Size;

/** Datos para crear una configuracion. El nombre es opcional. */
public record CreateConfigurationRequest(

        @Size(max = 120, message = "El nombre no puede superar 120 caracteres") String name) {
}
