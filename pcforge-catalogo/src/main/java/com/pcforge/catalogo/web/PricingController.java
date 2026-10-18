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

import com.pcforge.catalogo.service.PricingService;
import com.pcforge.catalogo.web.dto.CostRequest;
import com.pcforge.catalogo.web.dto.MarginResponse;
import com.pcforge.catalogo.web.dto.PricingPolicyRequest;
import com.pcforge.catalogo.web.dto.PricingPolicyResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Endpoints del analista de precios y margenes. El costo se registra con un
 * endpoint aparte y reservado al administrador: quien define la politica de
 * margen no debe capturar el costo que esa politica controla.
 */
@RestController
@RequestMapping("/api/v1/pricing")
@Tag(name = "Precios y margenes", description = "Politica por categoria, referencias bajo margen y costo")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(final PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/policy")
    @Operation(summary = "Politica de margen y descuento por categoria")
    public List<PricingPolicyResponse> policies(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return pricingService.policies(includeInactive);
    }

    @PutMapping("/policy/{categoryId}")
    @Operation(summary = "Fija el margen minimo y el umbral de descuento de una categoria")
    public PricingPolicyResponse updatePolicy(@PathVariable String categoryId,
            @Valid @RequestBody PricingPolicyRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String actorId = jwt != null ? jwt.getSubject() : null;
        return pricingService.updatePolicy(categoryId, request, actorId);
    }

    @GetMapping("/margins")
    @Operation(summary = "Margen por producto; con onlyAlerts devuelve solo lo que requiere decision")
    public List<MarginResponse> margins(@RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "false") boolean onlyAlerts,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return pricingService.margins(categoryId, onlyAlerts, includeInactive);
    }

    @GetMapping("/products/{id}/margin")
    @Operation(summary = "Margen de un producto")
    public MarginResponse margin(@PathVariable String id) {
        return pricingService.margin(id);
    }

    @PutMapping("/products/{id}/cost")
    @Operation(summary = "Registra el costo de adquisicion de un producto (solo administracion)")
    public MarginResponse updateCost(@PathVariable String id,
            @Valid @RequestBody CostRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String actorId = jwt != null ? jwt.getSubject() : null;
        return pricingService.updateCost(id, request, actorId);
    }
}
