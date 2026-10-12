package com.pcforge.compatibilidad.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.pcforge.compatibilidad.domain.Configuration;
import com.pcforge.compatibilidad.domain.ConfigurationItem;

/** Representacion de una configuracion armada. */
public record ConfigurationResponse(
        String id,
        String ownerId,
        String name,
        List<Item> items,
        BigDecimal estimatedTotal,
        String lastResult,
        Instant createdAt,
        Instant updatedAt) {

    public record Item(
            String itemId,
            String productId,
            String sku,
            String name,
            String componentType,
            BigDecimal price,
            int quantity,
            ConfigurationItem.ItemSpecs specs) {

        static Item from(ConfigurationItem i) {
            return new Item(i.getItemId(), i.getProductId(), i.getSku(), i.getName(),
                    i.getComponentType(), i.getPrice(), i.getQuantity(), i.getSpecs());
        }
    }

    public static ConfigurationResponse from(Configuration c) {
        BigDecimal total = c.getItems().stream()
                .map(i -> i.getPrice() == null ? BigDecimal.ZERO
                        : i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ConfigurationResponse(c.getId(), c.getOwnerId(), c.getName(),
                c.getItems().stream().map(Item::from).toList(), total, c.getLastResult(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
