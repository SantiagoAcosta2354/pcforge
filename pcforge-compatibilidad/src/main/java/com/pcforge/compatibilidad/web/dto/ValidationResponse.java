package com.pcforge.compatibilidad.web.dto;

import java.time.Instant;
import java.util.List;

import com.pcforge.compatibilidad.domain.CheckStatus;
import com.pcforge.compatibilidad.domain.RuleCode;
import com.pcforge.compatibilidad.domain.ValidationResult;

/**
 * Resultado de la validacion. Se entrega el detalle de cada regla, porque el
 * valor del configurador esta tanto en el veredicto como en su explicacion.
 */
public record ValidationResponse(
        String id,
        String configurationId,
        CheckStatus result,
        List<Check> checks,
        List<String> missingData,
        Instant createdAt) {

    public record Check(RuleCode rule, CheckStatus status, String message) {

        static Check from(ValidationResult.RuleCheck c) {
            return new Check(c.getRule(), c.getStatus(), c.getMessage());
        }
    }

    public static ValidationResponse from(ValidationResult v) {
        return new ValidationResponse(v.getId(), v.getConfigurationId(), v.getResult(),
                v.getChecks().stream().map(Check::from).toList(), v.getMissingData(), v.getCreatedAt());
    }
}
