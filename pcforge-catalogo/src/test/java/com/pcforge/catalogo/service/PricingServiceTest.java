package com.pcforge.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.web.dto.CostRequest;
import com.pcforge.catalogo.web.dto.MarginResponse;
import com.pcforge.catalogo.web.dto.PricingPolicyRequest;
import com.pcforge.catalogo.web.dto.PricingPolicyResponse;

/**
 * Reglas del analista de precios: el margen se mide sobre el precio de venta y
 * una referencia por debajo del minimo de su categoria queda en alerta.
 */
class PricingServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private PricingService service;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        categoryRepository = Mockito.mock(CategoryRepository.class);
        service = new PricingService(productRepository, categoryRepository,
                new BigDecimal("18"), new BigDecimal("5"));
    }

    @Test
    @DisplayName("Una referencia con margen menor al minimo de la categoria queda en alerta")
    void shouldFlagProductBelowMinimumMargin() {
        Category category = Category.builder().id("cat-cpu").name("Procesadores").slug("procesadores").active(true)
                .build();
        category.setMargenMinimo(new BigDecimal("18.00"));
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        Product product = Product.builder().id("p-1").sku("CPU-I9").name("Procesador de gama alta")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("1000.00")).build();
        product.setCosto(new BigDecimal("950.00"));
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<MarginResponse> margins = service.margins(null, true, false);

        assertThat(margins).hasSize(1);
        assertThat(margins.get(0).marginPercentage()).isEqualByComparingTo("5.00");
        assertThat(margins.get(0).status()).isEqualTo(MarginResponse.STATUS_BELOW);
        assertThat(margins.get(0).gap()).isEqualByComparingTo("13.00");
        assertThat(margins.get(0).policySource()).isEqualTo("categoria");
    }

    @Test
    @DisplayName("Una referencia conforme no aparece cuando se piden solo alertas")
    void shouldSkipProductWithHealthyMargin() {
        Category category = Category.builder().id("cat-ram").name("Memorias").slug("memorias-ram").active(true)
                .build();
        category.setMargenMinimo(new BigDecimal("15.00"));
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        Product product = Product.builder().id("p-2").sku("RAM-16").name("Memoria 16 GB")
                .categoryId("cat-ram").componentType(ComponentType.MEMORIA_RAM)
                .price(new BigDecimal("400.00")).build();
        product.setCosto(new BigDecimal("300.00"));
        when(productRepository.findAll()).thenReturn(List.of(product));

        assertThat(service.margins(null, true, false)).isEmpty();
        assertThat(service.margins(null, false, false)).hasSize(1);
    }

    @Test
    @DisplayName("Sin costo registrado el margen no se calcula y se informa la causa")
    void shouldReportMissingCost() {
        Category category = Category.builder().id("cat-cpu").name("Procesadores").slug("procesadores").active(true)
                .build();
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        Product product = Product.builder().id("p-3").sku("CPU-X").name("Procesador sin costo")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("500.00")).build();
        when(productRepository.findAll()).thenReturn(List.of(product));

        MarginResponse margin = service.margins(null, false, false).get(0);

        assertThat(margin.status()).isEqualTo(MarginResponse.STATUS_NO_COST);
        assertThat(margin.marginPercentage()).isNull();
        assertThat(margin.minimumMargin()).isEqualByComparingTo("18");
    }

    @Test
    @DisplayName("La politica registra responsable y fecha del cambio")
    void shouldRecordPolicyAuthor() {
        Category category = Category.builder().id("cat-cpu").name("Procesadores").slug("procesadores").active(true)
                .build();
        when(categoryRepository.findById("cat-cpu")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PricingPolicyResponse response = service.updatePolicy("cat-cpu",
                new PricingPolicyRequest(new BigDecimal("20"), new BigDecimal("4")), "analista-1");

        assertThat(response.minimumMargin()).isEqualByComparingTo("20.00");
        assertThat(response.policyUpdatedBy()).isEqualTo("analista-1");
        assertThat(response.policyUpdatedAt()).isNotNull();
        assertThat(category.getPolicyUpdatedAt()).isBefore(Instant.now().plusSeconds(5));
    }

    @Test
    @DisplayName("Un margen minimo fuera de rango se rechaza")
    void shouldRejectMarginOutOfRange() {
        Category category = Category.builder().id("cat-cpu").name("Procesadores").slug("procesadores").active(true)
                .build();
        when(categoryRepository.findById("cat-cpu")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.updatePolicy("cat-cpu",
                new PricingPolicyRequest(new BigDecimal("95"), null), "analista-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("margen minimo");
    }

    @Test
    @DisplayName("Al registrar el costo se devuelve el margen resultante")
    void shouldUpdateCostAndReturnMargin() {
        Category category = Category.builder().id("cat-cpu").name("Procesadores").slug("procesadores").active(true)
                .build();
        category.setMargenMinimo(new BigDecimal("18.00"));
        when(categoryRepository.findById("cat-cpu")).thenReturn(Optional.of(category));

        Product product = Product.builder().id("p-4").sku("CPU-Y").name("Procesador")
                .categoryId("cat-cpu").componentType(ComponentType.PROCESADOR)
                .price(new BigDecimal("1000.00")).build();
        when(productRepository.findById("p-4")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarginResponse response = service.updateCost("p-4", new CostRequest(new BigDecimal("800")), "admin-1");

        assertThat(product.getCosto()).isEqualByComparingTo("800.00");
        assertThat(response.marginPercentage()).isEqualByComparingTo("20.00");
        assertThat(response.status()).isEqualTo(MarginResponse.STATUS_OK);
    }
}
