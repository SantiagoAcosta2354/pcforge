package com.pcforge.usuarios.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import com.pcforge.usuarios.domain.User;

/**
 * El indice unico sobre el correo es la garantia a nivel de base de que no
 * existan dos cuentas con el mismo correo, incluso si dos peticiones llegan
 * al mismo tiempo. En Spring Boot 4 los indices se declaran de forma explicita.
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
            mongoTemplate.indexOps(User.class).ensureIndex(new Index().on("email", Sort.Direction.ASC).unique().named("uk_users_email"));
            log.info("Indices de MongoDB verificados correctamente");
        };
    }

    public MongoIndexConfig(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
