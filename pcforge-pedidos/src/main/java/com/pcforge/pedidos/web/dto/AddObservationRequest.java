package com.pcforge.pedidos.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Comentario de seguimiento sobre un pedido. */
public record AddObservationRequest(

        @NotBlank(message = "El texto de la observacion es obligatorio")
        @Size(max = 400, message = "La observacion no puede superar 400 caracteres")
        String text) {
}
