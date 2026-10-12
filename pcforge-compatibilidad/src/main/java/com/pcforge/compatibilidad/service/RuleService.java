package com.pcforge.compatibilidad.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pcforge.compatibilidad.domain.CompatibilityRule;
import com.pcforge.compatibilidad.exception.ResourceNotFoundException;
import com.pcforge.compatibilidad.repository.CompatibilityRuleRepository;
import com.pcforge.compatibilidad.web.dto.RuleResponse;
import com.pcforge.compatibilidad.web.dto.RuleUpdateRequest;

/**
 * Administracion de las reglas. Los parametros viven en la base de datos para
 * poder ajustarlos sin recompilar: el margen de potencia, por ejemplo, es una
 * decision de la tienda y no una constante del programa.
 */
@Service
public class RuleService {
    private final CompatibilityRuleRepository ruleRepository;

    public List<RuleResponse> findAll(boolean includeInactive) {
        List<CompatibilityRule> rules = includeInactive ? ruleRepository.findAll() : ruleRepository.findByActiveTrue();
        return rules.stream().map(RuleResponse::from).toList();
    }

    @Transactional
    public RuleResponse update(String id, RuleUpdateRequest request) {
        CompatibilityRule rule = ruleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe la regla " + id));
        if (request.active() != null) {
            rule.setActive(request.active());
        }
        if (request.parameters() != null) {
            rule.setParameters(request.parameters());
        }
        return RuleResponse.from(ruleRepository.save(rule));
    }

    public RuleService(final CompatibilityRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }
}
