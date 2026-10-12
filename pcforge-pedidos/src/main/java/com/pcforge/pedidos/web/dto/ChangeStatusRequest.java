package com.pcforge.pedidos.web.dto;

import com.pcforge.pedidos.domain.OrderStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cambio de estado del pedido, con la observacion que lo justifica. */
public record ChangeStatusRequest(

        @NotNull(message = "El estado destino es obligatorio")
        OrderStatus status,

        @Size(max = 400, message = "La observacion no puede superar 400 caracteres")
        String note,

        String technicianId,

        @Size(max = 120)
        String technicianName) {
}
