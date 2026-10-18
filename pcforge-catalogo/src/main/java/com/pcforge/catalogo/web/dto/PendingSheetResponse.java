package com.pcforge.catalogo.web.dto;

import java.time.Instant;
import java.util.List;

import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.TechnicalRequirements;

/**
 * Fila de la bandeja de fichas incompletas: lo minimo que el curador necesita
 * para decidir por donde empezar.
 */
public record PendingSheetResponse(
        String id,
        String sku,
        String name,
        String categoryId,
        String categoryName,
        ComponentType componentType,
        boolean active,
        List<String> missingAttributes,
        List<String> missingLabels,
        List<String> attributesWithoutSource,
        boolean blocksCompatibility,
        Instant updatedAt) {

    public static PendingSheetResponse from(Product p, String categoryName) {
        List<String> missing = TechnicalRequirements.missing(p);
        return new PendingSheetResponse(
                p.getId(), p.getSku(), p.getName(), p.getCategoryId(), categoryName,
                p.getComponentType(), p.isActive(), missing, TechnicalRequirements.labels(missing),
                TechnicalRequirements.missingSource(p), !missing.isEmpty(), p.getUpdatedAt());
    }
}
