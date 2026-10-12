package com.pcforge.compatibilidad.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.compatibilidad.domain.ValidationResult;

public interface ValidationResultRepository extends MongoRepository<ValidationResult, String> {

    List<ValidationResult> findByConfigurationIdOrderByCreatedAtDesc(String configurationId);
}
