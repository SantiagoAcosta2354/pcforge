package com.pcforge.usuarios.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Credenciales de acceso. */
public record LoginRequest(

        @NotBlank(message = "El correo es obligatorio") String email,
        @NotBlank(message = "La contrasena es obligatoria") String password) {
}
