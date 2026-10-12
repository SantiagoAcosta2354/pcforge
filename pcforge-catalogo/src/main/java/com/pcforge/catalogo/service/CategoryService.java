package com.pcforge.catalogo.service;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.exception.ResourceNotFoundException;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.web.dto.CategoryRequest;
import com.pcforge.catalogo.web.dto.CategoryResponse;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> findAll(boolean includeInactive) {
        List<Category> categories = includeInactive ? categoryRepository.findAll() : categoryRepository.findByActiveTrueOrderByNameAsc();
        return categories.stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse findBySlug(String slug) {
        return categoryRepository.findBySlug(normalizeSlug(slug)).map(CategoryResponse::from).orElseThrow(() -> new ResourceNotFoundException("No existe la categoria " + slug));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = normalizeSlug(request.slug());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("Ya existe una categoria con el slug " + slug);
        }
        Category category = Category.builder().name(request.name().trim()).slug(slug).description(request.description()).active(request.active() == null || request.active()).build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe la categoria " + id));
        String slug = normalizeSlug(request.slug());
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("Ya existe otra categoria con el slug " + slug);
        }
        category.setName(request.name().trim());
        category.setSlug(slug);
        category.setDescription(request.description());
        if (request.active() != null) {
            category.setActive(request.active());
        }
        return CategoryResponse.from(categoryRepository.save(category));
    }

    /**
     * Una categoria con productos asociados no se puede desactivar, porque
     * los productos quedarian sin clasificacion visible en el catalogo.
     */
    @Transactional
    public CategoryResponse changeStatus(String id, boolean active) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe la categoria " + id));
        if (!active && !productRepository.findByCategoryId(id).isEmpty()) {
            throw new BusinessRuleException("No se puede desactivar la categoria " + category.getName() + " porque tiene productos asociados");
        }
        category.setActive(active);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    private String normalizeSlug(String slug) {
        return slug == null ? null : slug.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    public CategoryService(final CategoryRepository categoryRepository, final ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }
}
