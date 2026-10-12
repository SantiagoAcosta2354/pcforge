package com.pcforge.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.pcforge.catalogo.domain.MovementType;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.StockMovement;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.exception.ResourceNotFoundException;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.repository.StockMovementRepository;
import com.pcforge.catalogo.web.dto.ProductResponse;
import com.pcforge.catalogo.web.dto.StockAdjustmentRequest;

class StockServiceTest {

    private ProductRepository productRepository;
    private StockMovementRepository stockMovementRepository;
    private StockService stockService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        stockMovementRepository = Mockito.mock(StockMovementRepository.class);
        stockService = new StockService(productRepository, stockMovementRepository);
    }

    @Test
    @DisplayName("Una salida que dejaria existencia negativa se rechaza y no deja movimiento")
    void shouldRejectOutputThatLeavesNegativeStock() {
        when(productRepository.findById("p-1")).thenReturn(Optional.of(
                Product.builder().id("p-1").sku("SKU-1").name("Producto").stock(2).active(true).build()));

        StockAdjustmentRequest request = new StockAdjustmentRequest(MovementType.SALIDA, 5, "Venta");

        assertThatThrownBy(() -> stockService.adjust("p-1", request, "user-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no puede ser negativa");

        verify(stockMovementRepository, never()).save(any(StockMovement.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Una entrada suma existencias y registra el movimiento con su actor")
    void shouldAddStockAndRegisterMovement() {
        when(productRepository.findById("p-1")).thenReturn(Optional.of(
                Product.builder().id("p-1").sku("SKU-1").name("Producto").stock(2).minimumStock(1)
                        .active(true).build()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = stockService.adjust("p-1",
                new StockAdjustmentRequest(MovementType.ENTRADA, 10, "Reposicion"), "user-9");

        assertThat(response.stock()).isEqualTo(12);

        var captor = org.mockito.ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getActorId()).isEqualTo("user-9");
        assertThat(captor.getValue().getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("Un ajuste fija el valor indicado, no lo suma")
    void shouldSetValueOnAdjustment() {
        when(productRepository.findById("p-1")).thenReturn(Optional.of(
                Product.builder().id("p-1").sku("SKU-1").name("Producto").stock(7).minimumStock(1)
                        .active(true).build()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = stockService.adjust("p-1",
                new StockAdjustmentRequest(MovementType.AJUSTE, 3, "Conteo fisico"), "user-9");

        assertThat(response.stock()).isEqualTo(3);
    }

    @Test
    @DisplayName("No se ajusta la existencia de un producto inexistente")
    void shouldFailWhenProductDoesNotExist() {
        when(productRepository.findById("p-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.adjust("p-x",
                new StockAdjustmentRequest(MovementType.ENTRADA, 1, null), "user-9"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
