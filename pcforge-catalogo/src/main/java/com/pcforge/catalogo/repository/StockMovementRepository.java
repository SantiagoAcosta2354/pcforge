package com.pcforge.catalogo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.catalogo.domain.StockMovement;

public interface StockMovementRepository extends MongoRepository<StockMovement, String> {

    List<StockMovement> findByProductIdOrderByOccurredAtDesc(String productId);
}
