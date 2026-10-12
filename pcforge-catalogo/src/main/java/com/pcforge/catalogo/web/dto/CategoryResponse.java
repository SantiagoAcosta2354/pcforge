package com.pcforge.catalogo.web.dto;

import com.pcforge.catalogo.domain.Category;

/** Representacion de una categoria en la API. */
public record CategoryResponse(
        String id,
        String name,
        String slug,
        String description,
        boolean active) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.isActive());
    }
}
