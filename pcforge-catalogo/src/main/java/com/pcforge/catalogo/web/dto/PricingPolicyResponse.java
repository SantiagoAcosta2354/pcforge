package com.pcforge.catalogo.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.pcforge.catalogo.domain.Category;

/** Politica de margen vigente para una categoria. */
public record PricingPolicyResponse(
        String categoryId,
        String categoryName,
        String slug,
        BigDecimal minimumMargin,
        BigDecimal discountThreshold,
        String policyUpdatedBy,
        Instant policyUpdatedAt,
        String policySource) {

    public static PricingPolicyResponse from(Category c, BigDecimal defaultMargin, BigDecimal defaultThreshold) {
        boolean ownPolicy = c.getMargenMinimo() != null;
        return new PricingPolicyResponse(
                c.getId(), c.getName(), c.getSlug(),
                ownPolicy ? c.getMargenMinimo() : defaultMargin,
                c.getUmbralDescuento() != null ? c.getUmbralDescuento() : defaultThreshold,
                c.getPolicyUpdatedBy(), c.getPolicyUpdatedAt(),
                ownPolicy ? "categoria" : "global");
    }
}
