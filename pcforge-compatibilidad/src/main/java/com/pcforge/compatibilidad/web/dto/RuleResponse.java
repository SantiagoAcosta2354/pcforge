package com.pcforge.compatibilidad.web.dto;

import java.util.Map;

import com.pcforge.compatibilidad.domain.CompatibilityRule;
import com.pcforge.compatibilidad.domain.RuleCode;

public record RuleResponse(
        String id,
        RuleCode code,
        String name,
        String description,
        boolean active,
        Map<String, String> parameters) {

    public static RuleResponse from(CompatibilityRule r) {
        return new RuleResponse(r.getId(), r.getCode(), r.getName(), r.getDescription(),
                r.isActive(), r.getParameters());
    }
}
