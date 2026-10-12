package com.pcforge.compatibilidad.client;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vista parcial de un producto del catalogo. Solo se declaran los campos que
 * este servicio necesita; los demas se ignoran al deserializar.
 */
public record CatalogoProduct(
        String id,
        String sku,
        String name,
        String componentType,
        BigDecimal price,
        int stock,
        boolean active,
        boolean available,
        Specs specs) {

    public record Specs(
            String socket,
            List<String> ramTypeSupported,
            String ramType,
            Integer estimatedPowerW,
            Integer capacityW,
            String formFactor) {
    }
}
