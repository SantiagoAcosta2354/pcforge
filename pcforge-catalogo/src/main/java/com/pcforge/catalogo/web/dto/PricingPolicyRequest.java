package com.pcforge.catalogo.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Politica comercial de una categoria. La fija el analista de precios y
 * margenes, no el administrador: el administrador mantiene el precio del dia
 * a dia, el analista define la regla que ese precio debe respetar.
 */
public record PricingPolicyRequest(

        @NotNull(message = "El margen minimo es obligatorio")
        @DecimalMin(value = "0.0", message = "El margen minimo no puede ser negativo")
        @DecimalMax(value = "90.0", message = "El margen minimo no puede superar 90 por ciento")
        BigDecimal minimumMargin,

        @DecimalMin(value = "0.0", message = "El umbral de descuento no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El umbral de descuento no puede superar 100 por ciento")
        BigDecimal discountThreshold) {
}
