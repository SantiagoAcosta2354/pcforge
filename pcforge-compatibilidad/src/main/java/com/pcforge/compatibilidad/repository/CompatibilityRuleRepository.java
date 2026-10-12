package com.pcforge.compatibilidad.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.compatibilidad.domain.CompatibilityRule;
import com.pcforge.compatibilidad.domain.RuleCode;

public interface CompatibilityRuleRepository extends MongoRepository<CompatibilityRule, String> {

    Optional<CompatibilityRule> findByCode(RuleCode code);

    boolean existsByCode(RuleCode code);

    List<CompatibilityRule> findByActiveTrue();
}
