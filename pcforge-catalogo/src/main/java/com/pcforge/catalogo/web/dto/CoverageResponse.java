package com.pcforge.catalogo.web.dto;

import java.util.List;

/**
 * Indicadores de calidad del catalogo: cuantas fichas estan completas y que
 * atributos son los que mas falta declarar. Es la pantalla de cobertura del
 * curador y, al mismo tiempo, la evidencia de que la validacion tiene con que
 * trabajar.
 */
public record CoverageResponse(
        int totalProducts,
        int completeSheets,
        int incompleteSheets,
        int blockingSheets,
        double coveragePercentage,
        List<CategoryCoverage> byCategory,
        List<MissingAttribute> topMissingAttributes) {

    public record CategoryCoverage(
            String categoryId,
            String categoryName,
            int totalProducts,
            int completeSheets,
            int incompleteSheets,
            double coveragePercentage) {
    }

    public record MissingAttribute(String attribute, String label, int productsAffected) {
    }
}
