package com.pcforge.compatibilidad.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Habilita @CreatedDate y @LastModifiedDate. */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
