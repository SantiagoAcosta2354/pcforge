package com.pcforge.catalogo.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.pcforge.catalogo.domain.AttributeSource;
import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.FichaStatus;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.TechnicalRequirements;
import com.pcforge.catalogo.domain.TechnicalSpecs;

/** Ficha tecnica de un producto tal como la ve el curador de datos. */
public record TechnicalDataResponse(
        String id,
        String sku,
        String name,
        String brand,
        String categoryId,
        String categoryName,
        ComponentType componentType,
        boolean active,
        TechnicalSpecs specs,
        Map<String, AttributeSource> sources,
        List<String> requiredAttributes,
        List<String> missingAttributes,
        List<String> attributesWithoutSource,
        FichaStatus fichaStatus,
        boolean blocksCompatibility,
        Instant verifiedAt,
        String verifiedBy,
        Instant updatedAt) {

    public static TechnicalDataResponse from(Product p, String categoryName) {
        List<String> missing = TechnicalRequirements.missing(p);
        return new TechnicalDataResponse(
                p.getId(), p.getSku(), p.getName(), p.getBrand(), p.getCategoryId(), categoryName,
                p.getComponentType(), p.isActive(), p.getSpecs(), p.getAttributeSources(),
                TechnicalRequirements.required(p.getComponentType()), missing,
                TechnicalRequirements.missingSource(p), TechnicalRequirements.status(p),
                !missing.isEmpty(), p.getVerifiedAt(), p.getVerifiedBy(), p.getUpdatedAt());
    }
}
