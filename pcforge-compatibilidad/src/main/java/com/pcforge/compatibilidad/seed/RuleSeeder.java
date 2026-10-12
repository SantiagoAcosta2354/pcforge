package com.pcforge.compatibilidad.seed;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import com.pcforge.compatibilidad.domain.CompatibilityRule;
import com.pcforge.compatibilidad.domain.RuleCode;
import com.pcforge.compatibilidad.repository.CompatibilityRuleRepository;

/**
 * Registra las tres reglas con sus parametros por defecto. Se crean una sola
 * vez: si ya existen no se sobrescriben, porque el administrador puede
 * haber ajustado el margen de potencia desde la API.
 */
@Configuration
@ConditionalOnProperty(name = "pcforge.seed.enabled", havingValue = "true")
public class RuleSeeder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RuleSeeder.class);
    private final CompatibilityRuleRepository ruleRepository;
    @Value("${pcforge.rules.power-margin-percent:20}")
    private String powerMarginPercent;

    @Bean
    @Order(2)
    ApplicationRunner seedRules() {
        return args -> {
            createIfAbsent(RuleCode.SOCKET, "Coincidencia de socket", "Compara el socket declarado del procesador con el de la placa base.", null);
            createIfAbsent(RuleCode.RAM_TYPE, "Tipo de memoria admitido", "Verifica que el tipo de cada modulo de memoria figure entre los que admite la placa base.", null);
            createIfAbsent(RuleCode.POWER, "Potencia estimada frente a la fuente", "Suma los consumos declarados, aplica un margen y lo compara con la capacidad de la fuente.", Map.of("marginPercent", powerMarginPercent));
            log.info("Reglas de compatibilidad verificadas");
        };
    }

    private void createIfAbsent(RuleCode code, String name, String description, Map<String, String> parameters) {
        if (ruleRepository.existsByCode(code)) {
            return;
        }
        ruleRepository.save(CompatibilityRule.builder().code(code).name(name).description(description).active(true).parameters(parameters).build());
    }

    public RuleSeeder(final CompatibilityRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }
}
