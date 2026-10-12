package com.pcforge.usuarios.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro publico. El usuario que se registra siempre queda con el rol
 * CLIENTE: los demas roles solo los asigna un administrador.
 */
public record RegisterRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String fullName,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 160)
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
        String password) {
}
