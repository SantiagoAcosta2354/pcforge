package com.pcforge.catalogo.web.dto;

import java.math.BigDecimal;

import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.TechnicalSpecs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos que se reciben al crear o actualizar un producto. */
public record ProductRequest(

        @NotBlank(message = "El SKU es obligatorio")
        @Size(max = 40, message = "El SKU no puede superar 40 caracteres")
        String sku,

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 140, message = "El nombre no puede superar 140 caracteres")
        String name,

        @Size(max = 80, message = "La marca no puede superar 80 caracteres")
        String brand,

        @NotBlank(message = "La categoria es obligatoria")
        String categoryId,

        @NotNull(message = "El tipo de componente es obligatorio")
        ComponentType componentType,

        @Size(max = 600, message = "La descripcion no puede superar 600 caracteres")
        String description,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        BigDecimal price,

        @Min(value = 0, message = "La existencia no puede ser negativa")
        int stock,

        @Min(value = 0, message = "El minimo de existencia no puede ser negativo")
        Integer minimumStock,

        Boolean active,

        TechnicalSpecs specs) {
}
