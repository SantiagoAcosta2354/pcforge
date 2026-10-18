package com.pcforge.catalogo.web.dto;

import java.math.BigDecimal;

/**
 * Situacion de margen de un producto. Incluye el costo, que por eso mismo no
 * se expone en la consulta publica del catalogo.
 */
public record MarginResponse(
        String productId,
        String sku,
        String name,
        String categoryId,
        String categoryName,
        boolean active,
        BigDecimal cost,
        BigDecimal price,
        BigDecimal marginPercentage,
        BigDecimal minimumMargin,
        BigDecimal gap,
        String status,
        String policySource) {

    public static final String STATUS_NO_COST = "SIN_COSTO";
    public static final String STATUS_BELOW = "BAJO_MARGEN";
    public static final String STATUS_OK = "CONFORME";
}
