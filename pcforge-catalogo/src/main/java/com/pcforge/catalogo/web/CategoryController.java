package com.pcforge.catalogo.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.pcforge.catalogo.service.CategoryService;
import com.pcforge.catalogo.web.dto.CategoryRequest;
import com.pcforge.catalogo.web.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categorias", description = "Clasificacion comercial de los productos")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lista las categorias del catalogo")
    public List<CategoryResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return categoryService.findAll(includeInactive);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Consulta una categoria por su slug")
    public CategoryResponse findBySlug(@PathVariable String slug) {
        return categoryService.findBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una categoria nueva")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoria existente")
    public CategoryResponse update(@PathVariable String id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activa o desactiva una categoria")
    public CategoryResponse changeStatus(@PathVariable String id, @RequestParam boolean active) {
        return categoryService.changeStatus(id, active);
    }

    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }
}
