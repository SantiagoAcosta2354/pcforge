package com.pcforge.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.FichaStatus;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.TechnicalSpecs;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.web.dto.TechnicalDataRequest;
import com.pcforge.catalogo.web.dto.TechnicalDataResponse;

/**
 * Reglas del curador de datos tecnicos: la bandeja prioriza lo que bloquea una
 * regla y no se declara completa una ficha sin atributos o sin fuente.
 */
class CatalogQualityServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private CatalogQualityService service;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        categoryRepository = Mockito.mock(CategoryRepository.class);
        service = new CatalogQualityService(productRepository, categoryRepository);
        when(categoryRepository.findAll()).thenReturn(List.of(
                Category.builder().id("cat-cpu").name("Procesadores").slug("procesadores").active(true).build()));
        when(categoryRepository.findById("cat-cpu")).thenReturn(Optional.of(
                Category.builder().id("cat-cpu").name("Procesadores").slug("procesadores").active(true).build()));
    }

    @Test
    @DisplayName("La bandeja lista solo las fichas con atributos obligatorios faltantes")
    void shouldListOnlyIncompleteSheets() {
        Product complete = Product.builder().id("p-1").sku("CPU-OK").name("Procesador completo")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("100.00"))
                .specs(TechnicalSpecs.builder().socket("AM5").estimatedPowerW(105).build()).build();
        Product incomplete = Product.builder().id("p-2").sku("CPU-SIN-SOCKET").name("Procesador sin socket")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("100.00"))
                .specs(TechnicalSpecs.builder().estimatedPowerW(65).build()).build();
        when(productRepository.findAll()).thenReturn(List.of(complete, incomplete));

        var pending = service.pending(false, false);

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).sku()).isEqualTo("CPU-SIN-SOCKET");
        assertThat(pending.get(0).missingAttributes()).containsExactly("socket");
        assertThat(pending.get(0).blocksCompatibility()).isTrue();
    }

    @Test
    @DisplayName("No se declara completa una ficha a la que le falta un atributo obligatorio")
    void shouldRejectCompleteDeclarationWhenAttributeIsMissing() {
        Product product = Product.builder().id("p-1").sku("CPU-SIN-SOCKET").name("Procesador")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("100.00"))
                .specs(TechnicalSpecs.builder().estimatedPowerW(65).build()).build();
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));

        TechnicalDataRequest request = new TechnicalDataRequest(product.getSpecs(), Map.of(), true);

        assertThatThrownBy(() -> service.updateAttributes("p-1", request, "curador-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("socket");
    }

    @Test
    @DisplayName("No se declara completa una ficha cuyos atributos no tienen fuente")
    void shouldRejectCompleteDeclarationWithoutSource() {
        Product product = Product.builder().id("p-1").sku("CPU-AM5").name("Procesador")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("100.00"))
                .specs(TechnicalSpecs.builder().socket("AM5").estimatedPowerW(105).build()).build();
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));

        TechnicalDataRequest request = new TechnicalDataRequest(product.getSpecs(), Map.of(), true);

        assertThatThrownBy(() -> service.updateAttributes("p-1", request, "curador-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("fuente");
    }

    @Test
    @DisplayName("Al registrar atributos con fuente, la ficha queda completa y verificada")
    void shouldCompleteSheetWithSource() {
        Product product = Product.builder().id("p-1").sku("CPU-AM5").name("Procesador")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("100.00"))
                .specs(TechnicalSpecs.builder().estimatedPowerW(105).build()).build();
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TechnicalSpecs fixed = TechnicalSpecs.builder().socket("AM5").estimatedPowerW(105).build();
        Map<String, String> sources = Map.of(
                "socket", "https://fabricante.example/cpu-am5",
                "estimatedPowerW", "https://fabricante.example/cpu-am5-tdp");
        TechnicalDataRequest request = new TechnicalDataRequest(fixed, sources, true);

        TechnicalDataResponse response = service.updateAttributes("p-1", request, "curador-1");

        assertThat(response.fichaStatus()).isEqualTo(FichaStatus.COMPLETA);
        assertThat(response.missingAttributes()).isEmpty();
        assertThat(response.verifiedBy()).isEqualTo("curador-1");
        assertThat(response.verifiedAt()).isNotNull();
        assertThat(response.sources()).containsKeys("socket", "estimatedPowerW");
    }
}
