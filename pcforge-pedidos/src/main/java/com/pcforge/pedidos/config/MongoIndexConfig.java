package com.pcforge.pedidos.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import com.pcforge.pedidos.domain.Counter;

/**
 * El indice unico del numero de pedido es lo que impide en la base que dos
 * solicitudes compartan el mismo numero legible.
 *
 * Nota: la entidad Order se referencia con su nombre completo porque su
 * nombre simple choca con la anotacion @Order de Spring.
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
            Class<com.pcforge.pedidos.domain.Order> orders = com.pcforge.pedidos.domain.Order.class;
            mongoTemplate.indexOps(orders).ensureIndex(new Index().on("orderNumber", Sort.Direction.ASC).unique().named("uk_orders_number"));
            mongoTemplate.indexOps(orders).ensureIndex(new Index().on("customerId", Sort.Direction.ASC).named("ix_orders_customer"));
            mongoTemplate.indexOps(orders).ensureIndex(new Index().on("status", Sort.Direction.ASC).named("ix_orders_status"));
            mongoTemplate.indexOps(Counter.class).ensureIndex(new Index().on("seq", Sort.Direction.ASC).named("ix_counters_seq"));
            log.info("Indices de MongoDB verificados correctamente");
        };
    }

    public MongoIndexConfig(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
