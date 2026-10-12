package com.pcforge.usuarios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Habilita @CreatedDate y @LastModifiedDate en las entidades. */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
