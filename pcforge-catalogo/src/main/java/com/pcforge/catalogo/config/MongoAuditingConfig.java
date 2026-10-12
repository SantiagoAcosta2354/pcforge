package com.pcforge.catalogo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Habilita las anotaciones @CreatedDate y @LastModifiedDate, de modo que la
 * fecha de creacion y de ultima modificacion de un producto se llenen solas.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
