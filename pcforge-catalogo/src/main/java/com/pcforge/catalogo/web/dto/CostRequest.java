package com.pcforge.catalogo.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Costo de adquisicion de un producto. Lo registra el administrador; el
 * analista de precios solo lo consulta para calcular el margen, de modo que
 * quien fija la politica no es quien captura el dato que la alimenta.
 */
public record CostRequest(

        @NotNull(message = "El costo es obligatorio")
        @DecimalMin(value = "0.01", message = "El costo debe ser mayor que cero")
        BigDecimal cost) {
}
