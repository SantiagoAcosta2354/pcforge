package com.pcforge.catalogo.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pcforge.catalogo.service.CatalogQualityService;
import com.pcforge.catalogo.web.dto.CoverageResponse;
import com.pcforge.catalogo.web.dto.PendingSheetResponse;
import com.pcforge.catalogo.web.dto.TechnicalDataRequest;
import com.pcforge.catalogo.web.dto.TechnicalDataResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Endpoints del curador de datos tecnicos. Ninguno permite modificar precio,
 * existencia ni estado del producto: el rol tiene su propio alcance y el
 * servidor lo aplica, no la pantalla.
 */
@RestController
@RequestMapping("/api/v1/catalog-quality")
@Tag(name = "Calidad del catalogo", description = "Fichas tecnicas incompletas, fuentes del dato y cobertura")
public class CatalogQualityController {

    private final CatalogQualityService catalogQualityService;

    public CatalogQualityController(final CatalogQualityService catalogQualityService) {
        this.catalogQualityService = catalogQualityService;
    }

    @GetMapping("/pending")
    @Operation(summary = "Bandeja de fichas con atributos faltantes; con includeUnverified suma las que no tienen fuente")
    public List<PendingSheetResponse> pending(@RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "false") boolean includeUnverified) {
        return catalogQualityService.pending(includeInactive, includeUnverified);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Ficha tecnica de un producto con su trazabilidad")
    public TechnicalDataResponse find(@PathVariable String id) {
        return catalogQualityService.find(id);
    }

    @PutMapping("/products/{id}/attributes")
    @Operation(summary = "Registra atributos tecnicos y su fuente; permite declarar la ficha completa")
    public TechnicalDataResponse updateAttributes(@PathVariable String id,
            @Valid @RequestBody TechnicalDataRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String actorId = jwt != null ? jwt.getSubject() : null;
        return catalogQualityService.updateAttributes(id, request, actorId);
    }

    @GetMapping("/coverage")
    @Operation(summary = "Porcentaje de fichas completas y atributos que mas faltan")
    public CoverageResponse coverage() {
        return catalogQualityService.coverage();
    }
}
