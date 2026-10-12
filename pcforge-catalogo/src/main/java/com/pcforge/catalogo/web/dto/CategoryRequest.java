package com.pcforge.catalogo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos que se reciben al crear o actualizar una categoria. */
public record CategoryRequest(

        @NotBlank(message = "El nombre de la categoria es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
        String name,

        @NotBlank(message = "El slug es obligatorio")
        @Size(max = 80, message = "El slug no puede superar 80 caracteres")
        String slug,

        @Size(max = 300, message = "La descripcion no puede superar 300 caracteres")
        String description,

        Boolean active) {
}
