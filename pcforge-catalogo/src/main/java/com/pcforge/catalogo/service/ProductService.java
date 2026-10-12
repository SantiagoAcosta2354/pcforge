package com.pcforge.catalogo.service;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.exception.ResourceNotFoundException;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.web.dto.ProductRequest;
import com.pcforge.catalogo.web.dto.ProductResponse;

/**
 * Reglas de negocio del catalogo e inventario.
 * Ninguna otra parte del sistema consulta estas colecciones directamente:
 * la informacion sale por la API REST de este servicio.
 */
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ---------------------------------------------------------------- consultas
    public List<ProductResponse> search(String categoryId, ComponentType componentType, String name, boolean includeInactive) {
        List<Product> found;
        if (name != null && !name.isBlank()) {
            found = productRepository.findByActiveTrueAndNameContainingIgnoreCase(name.trim());
            if (categoryId != null && !categoryId.isBlank()) {
                found = found.stream().filter(p -> categoryId.equals(p.getCategoryId())).toList();
            }
        } else if (categoryId != null && !categoryId.isBlank()) {
            found = productRepository.findByActiveTrueAndCategoryId(categoryId);
        } else if (componentType != null) {
            found = productRepository.findByActiveTrueAndComponentType(componentType);
        } else if (includeInactive) {
            found = productRepository.findAll();
        } else {
            found = productRepository.findByActiveTrueOrderByNameAsc();
        }
        return found.stream().map(ProductResponse::from).toList();
    }

    public ProductResponse findById(String id) {
        return ProductResponse.from(requireProduct(id));
    }

    public ProductResponse findBySku(String sku) {
        return productRepository.findBySku(normalizeSku(sku)).map(ProductResponse::from).orElseThrow(() -> new ResourceNotFoundException("No existe un producto con el SKU " + sku));
    }

    /**
     * Productos activos que ya alcanzaron o bajaron de su minimo definido.
     */
    public List<ProductResponse> lowStock() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream().filter(p -> p.getStock() <= p.getMinimumStock()).map(ProductResponse::from).toList();
    }

    // ---------------------------------------------------------------- comandos
    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = normalizeSku(request.sku());
        if (productRepository.existsBySku(sku)) {
            throw new BusinessRuleException("Ya existe un producto registrado con el SKU " + sku);
        }
        requireActiveCategory(request.categoryId());
        Product product = Product.builder().sku(sku).name(request.name().trim()).brand(request.brand()).categoryId(request.categoryId()).componentType(request.componentType()).description(request.description()).price(request.price()).stock(request.stock()).minimumStock(request.minimumStock() == null ? 1 : request.minimumStock()).active(request.active() == null || request.active()).specs(request.specs()).build();
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(String id, ProductRequest request) {
        Product product = requireProduct(id);
        String sku = normalizeSku(request.sku());
        if (!product.getSku().equals(sku) && productRepository.existsBySku(sku)) {
            throw new BusinessRuleException("Ya existe otro producto con el SKU " + sku);
        }
        requireActiveCategory(request.categoryId());
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setBrand(request.brand());
        product.setCategoryId(request.categoryId());
        product.setComponentType(request.componentType());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        if (request.minimumStock() != null) {
            product.setMinimumStock(request.minimumStock());
        }
        if (request.active() != null) {
            product.setActive(request.active());
        }
        product.setSpecs(request.specs());
        return ProductResponse.from(productRepository.save(product));
    }

    /**
     * No se elimina el producto: se desactiva. Un producto historico no debe
     * desaparecer porque los pedidos anteriores guardan su referencia.
     */
    @Transactional
    public ProductResponse changeStatus(String id, boolean active) {
        Product product = requireProduct(id);
        product.setActive(active);
        return ProductResponse.from(productRepository.save(product));
    }

    // ---------------------------------------------------------------- apoyo
    public Product requireProduct(String id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe un producto con el id " + id));
    }

    private void requireActiveCategory(String categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("No existe la categoria " + categoryId));
        if (!category.isActive()) {
            throw new BusinessRuleException("La categoria " + category.getName() + " esta inactiva");
        }
    }

    private String normalizeSku(String sku) {
        return sku == null ? null : sku.trim().toUpperCase(Locale.ROOT);
    }

    public ProductService(final ProductRepository productRepository, final CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
}
