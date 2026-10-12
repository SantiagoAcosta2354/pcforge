package com.pcforge.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
 * Pruebas de las reglas de negocio del catalogo. Se trabaja con repositorios
 * simulados para no depender de una base de datos en ejecucion.
 */
class ProductServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        categoryRepository = Mockito.mock(CategoryRepository.class);
        productService = new ProductService(productRepository, categoryRepository);
    }

    @Test
    @DisplayName("No se permite registrar dos productos con el mismo SKU")
    void shouldRejectDuplicatedSku() {
        when(productRepository.existsBySku("CPU-AM5-7600")).thenReturn(true);

        ProductRequest request = new ProductRequest("cpu-am5-7600", "Procesador", "AMD", "cat-1",
                ComponentType.PROCESADOR, "descripcion", new BigDecimal("100.00"), 1, 1, true, null);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CPU-AM5-7600");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("No se permite registrar un producto en una categoria inexistente")
    void shouldRejectUnknownCategory() {
        when(productRepository.existsBySku(any())).thenReturn(false);
        when(categoryRepository.findById("cat-x")).thenReturn(Optional.empty());

        ProductRequest request = new ProductRequest("SKU-1", "Producto", "Marca", "cat-x",
                ComponentType.PROCESADOR, "descripcion", new BigDecimal("10.00"), 1, 1, true, null);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Un producto se guarda normalizado y disponible si tiene existencias")
    void shouldCreateProductWithNormalizedSku() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(
                Category.builder().id("cat-1").name("Procesadores").slug("procesadores").active(true).build()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = new ProductRequest(" sku-1 ", "Procesador", "AMD", "cat-1",
                ComponentType.PROCESADOR, "descripcion", new BigDecimal("100.00"), 4, 1, true, null);

        ProductResponse response = productService.create(request);

        assertThat(response.sku()).isEqualTo("SKU-1");
        assertThat(response.available()).isTrue();
        assertThat(response.lowStock()).isFalse();
    }

    @Test
    @DisplayName("Un producto inactivo no se ofrece aunque tenga existencias")
    void shouldNotOfferInactiveProduct() {
        when(productRepository.findById("p-1")).thenReturn(Optional.of(Product.builder()
                .id("p-1").sku("SKU-2").name("Producto inactivo").stock(10).active(false).build()));

        ProductResponse response = productService.findById("p-1");

        assertThat(response.active()).isFalse();
        assertThat(response.available()).isFalse();
    }
}
