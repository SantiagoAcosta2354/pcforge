package com.pcforge.compatibilidad.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.pcforge.compatibilidad.service.RuleService;
import com.pcforge.compatibilidad.web.dto.RuleResponse;
import com.pcforge.compatibilidad.web.dto.RuleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/rules")
@Tag(name = "Reglas", description = "Parametros de las reglas de compatibilidad")
public class RuleController {
    private final RuleService ruleService;

    @GetMapping
    @Operation(summary = "Lista las reglas de compatibilidad")
    public List<RuleResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return ruleService.findAll(includeInactive);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Ajusta el estado o los parametros de una regla (solo administrador)")
    public RuleResponse update(@PathVariable String id, @RequestBody RuleUpdateRequest request) {
        return ruleService.update(id, request);
    }

    public RuleController(final RuleService ruleService) {
        this.ruleService = ruleService;
    }
}
