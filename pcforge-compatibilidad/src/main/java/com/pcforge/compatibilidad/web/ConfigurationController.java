package com.pcforge.compatibilidad.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.pcforge.compatibilidad.config.CallerResolver;
import com.pcforge.compatibilidad.service.CompatibilityService;
import com.pcforge.compatibilidad.web.dto.AddItemRequest;
import com.pcforge.compatibilidad.web.dto.ConfigurationResponse;
import com.pcforge.compatibilidad.web.dto.CreateConfigurationRequest;
import com.pcforge.compatibilidad.web.dto.ValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/configurations")
@Tag(name = "Configurador", description = "Armado de equipos y validacion de compatibilidad")
public class ConfigurationController {
    private final CompatibilityService compatibilityService;
    private final CallerResolver callerResolver;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una configuracion de equipos")
    public ConfigurationResponse create(@Valid @RequestBody CreateConfigurationRequest request, @AuthenticationPrincipal Jwt jwt) {
        return compatibilityService.create(callerResolver.from(jwt), request.name());
    }

    @GetMapping
    @Operation(summary = "Lista las configuraciones propias (el personal ve todas)")
    public List<ConfigurationResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return compatibilityService.list(callerResolver.from(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una configuracion")
    public ConfigurationResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return compatibilityService.get(id, callerResolver.from(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una configuracion propia")
    public void delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        compatibilityService.delete(id, callerResolver.from(jwt));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Agrega un componente del catalogo a la configuracion")
    public ConfigurationResponse addItem(@PathVariable String id, @Valid @RequestBody AddItemRequest request, @AuthenticationPrincipal Jwt jwt) {
        return compatibilityService.addItem(id, request.productId(), request.quantity(), callerResolver.from(jwt));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Retira un componente de la configuracion")
    public ConfigurationResponse removeItem(@PathVariable String id, @PathVariable String itemId, @AuthenticationPrincipal Jwt jwt) {
        return compatibilityService.removeItem(id, itemId, callerResolver.from(jwt));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Ejecuta las reglas de compatibilidad sobre la configuracion")
    public ValidationResponse validate(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return compatibilityService.validate(id, callerResolver.from(jwt));
    }

    @GetMapping("/{id}/validations")
    @Operation(summary = "Historial de validaciones de la configuracion")
    public List<ValidationResponse> validations(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return compatibilityService.history(id, callerResolver.from(jwt));
    }

    public ConfigurationController(final CompatibilityService compatibilityService, final CallerResolver callerResolver) {
        this.compatibilityService = compatibilityService;
        this.callerResolver = callerResolver;
    }
}
