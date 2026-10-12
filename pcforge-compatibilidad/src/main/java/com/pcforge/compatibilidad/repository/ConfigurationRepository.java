package com.pcforge.compatibilidad.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.compatibilidad.domain.Configuration;

public interface ConfigurationRepository extends MongoRepository<Configuration, String> {

    List<Configuration> findByOwnerIdOrderByUpdatedAtDesc(String ownerId);

    long countByOwnerId(String ownerId);
}
