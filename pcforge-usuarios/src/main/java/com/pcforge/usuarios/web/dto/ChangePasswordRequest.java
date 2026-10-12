package com.pcforge.usuarios.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cambio de la propia contrasena. Exige la contrasena actual. */
public record ChangePasswordRequest(

        @NotBlank(message = "La contrasena actual es obligatoria") String currentPassword,

        @NotBlank(message = "La contrasena nueva es obligatoria")
        @Size(min = 8, max = 72, message = "La contrasena nueva debe tener entre 8 y 72 caracteres")
        String newPassword) {
}
