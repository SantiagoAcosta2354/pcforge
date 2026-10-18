package com.pcforge.catalogo.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.FichaStatus;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.TechnicalRequirements;
import com.pcforge.catalogo.domain.TechnicalSpecs;

/**
 * Representacion de un producto tal como se expone en la API.
 * No incluye el costo: ese dato solo lo ve el perfil de precios y margenes.
 */
public record ProductResponse(
        String id,
        String sku,
        String name,
        String brand,
        String categoryId,
        ComponentType componentType,
        String description,
        BigDecimal price,
        int stock,
        int minimumStock,
        boolean active,
        boolean available,
        boolean lowStock,
        TechnicalSpecs specs,
        FichaStatus fichaStatus,
        List<String> missingAttributes,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getSku(), p.getName(), p.getBrand(), p.getCategoryId(),
                p.getComponentType(), p.getDescription(), p.getPrice(), p.getStock(),
                p.getMinimumStock(), p.isActive(), p.isAvailable(),
                p.isActive() && p.getStock() <= p.getMinimumStock(),
                p.getSpecs(), TechnicalRequirements.status(p),
                TechnicalRequirements.missing(p), p.getCreatedAt(), p.getUpdatedAt());
    }
}
