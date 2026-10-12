package com.pcforge.compatibilidad.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import com.pcforge.compatibilidad.domain.CompatibilityRule;
import com.pcforge.compatibilidad.domain.ValidationResult;

/**
 * Indices propios del servicio. El unico sobre el codigo de regla impide que
 * existan dos definiciones de la misma regla.
 */
@Configuration
@ConditionalOnProperty(name = "pcforge.indexes.enabled", havingValue = "true", matchIfMissing = true)
public class MongoIndexConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MongoIndexConfig.class);
    private final MongoTemplate mongoTemplate;

    @Bean
    @Order(1)
    ApplicationRunner ensureIndexes() {
        return args -> {
            mongoTemplate.indexOps(CompatibilityRule.class).ensureIndex(new Index().on("code", Sort.Direction.ASC).unique().named("uk_rules_code"));
            // Nombre completo: la clase de dominio Configuration tiene el mismo
            // nombre simple que la anotacion @Configuration de Spring.
            mongoTemplate.indexOps(com.pcforge.compatibilidad.domain.Configuration.class).ensureIndex(new Index().on("ownerId", Sort.Direction.ASC).named("ix_configurations_owner"));
            mongoTemplate.indexOps(ValidationResult.class).ensureIndex(new Index().on("configurationId", Sort.Direction.ASC).named("ix_validations_configuration"));
            log.info("Indices de MongoDB verificados correctamente");
        };
    }

    public MongoIndexConfig(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
