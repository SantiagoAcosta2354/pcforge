package com.pcforge.catalogo.web.dto;

import com.pcforge.catalogo.domain.MovementType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de ajuste de existencia. El tipo de movimiento indica el sentido:
 * ENTRADA suma, SALIDA resta y AJUSTE fija el valor indicado.
 */
public record StockAdjustmentRequest(

        @NotNull(message = "El tipo de movimiento es obligatorio")
        MovementType movementType,

        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        int quantity,

        @Size(max = 200, message = "El motivo no puede superar 200 caracteres")
        String reason) {
}
