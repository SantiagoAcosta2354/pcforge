package com.pcforge.compatibilidad.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Agrega un componente del catalogo a la configuracion. */
public record AddItemRequest(

        @NotBlank(message = "El identificador del producto es obligatorio") String productId,

        @Min(value = 1, message = "La cantidad debe ser mayor que cero") Integer quantity) {
}
