package com.pcforge.catalogo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.catalogo.domain.Category;

public interface CategoryRepository extends MongoRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByActiveTrue();

    List<Category> findByActiveTrueOrderByNameAsc();
}
