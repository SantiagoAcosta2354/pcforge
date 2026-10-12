package com.pcforge.catalogo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.Product;

public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByActiveTrueOrderByNameAsc();

    List<Product> findByActiveTrueAndCategoryId(String categoryId);

    List<Product> findByActiveTrueAndComponentType(ComponentType componentType);

    List<Product> findByActiveTrueAndNameContainingIgnoreCase(String name);

    /** Productos activos cuya existencia ya alcanzo el minimo definido. */
    List<Product> findByActiveTrueAndStockLessThanEqual(int stock);

    List<Product> findByCategoryId(String categoryId);
}
