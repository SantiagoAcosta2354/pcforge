package com.pcforge.catalogo.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.StockMovement;

/**
 * Crea los indices de forma explicita.
 * En Spring Boot 4 la creacion automatica de indices ya no se controla con
 * spring.data.mongodb.auto-index-creation, asi que se declaran aqui. Los
 * indices unicos son los que garantizan a nivel de base que no se repitan
 * el SKU de un producto ni el slug de una categoria.
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
            mongoTemplate.indexOps(Product.class).ensureIndex(new Index().on("sku", Sort.Direction.ASC).unique().named("uk_products_sku"));
            mongoTemplate.indexOps(Product.class).ensureIndex(new Index().on("componentType", Sort.Direction.ASC).named("ix_products_component_type"));
            mongoTemplate.indexOps(Category.class).ensureIndex(new Index().on("slug", Sort.Direction.ASC).unique().named("uk_categories_slug"));
            mongoTemplate.indexOps(StockMovement.class).ensureIndex(new Index().on("productId", Sort.Direction.ASC).named("ix_stock_movements_product"));
            log.info("Indices de MongoDB verificados correctamente");
        };
    }

    public MongoIndexConfig(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
