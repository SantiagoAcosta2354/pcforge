package com.pcforge.catalogo.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pcforge.catalogo.domain.MovementType;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.StockMovement;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.repository.StockMovementRepository;
import com.pcforge.catalogo.web.dto.ProductResponse;
import com.pcforge.catalogo.web.dto.StockAdjustmentRequest;
import com.pcforge.catalogo.web.dto.StockMovementResponse;

/**
 * Control de existencias. Cada cambio deja un movimiento registrado, de modo
 * que el stock actual siempre pueda explicarse a partir de su historial.
 */
@Service
public class StockService {
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public ProductResponse adjust(String productId, StockAdjustmentRequest request, String actorId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new com.pcforge.catalogo.exception.ResourceNotFoundException("No existe un producto con el id " + productId));
        int quantity = request.quantity();
        int resultingStock = switch (request.movementType()) {
            case ENTRADA -> product.getStock() + quantity;
            case SALIDA -> product.getStock() - quantity;
            case AJUSTE -> quantity;
        };
        if (resultingStock < 0) {
            throw new BusinessRuleException("La salida de " + quantity + " unidades dejaria la existencia en " + resultingStock + ", y la existencia no puede ser negativa");
        }
        product.setStock(resultingStock);
        Product saved = productRepository.save(product);
        stockMovementRepository.save(StockMovement.builder().productId(product.getId()).movementType(request.movementType()).quantity(quantity).reason(normalizeReason(request)).actorId(actorId).occurredAt(Instant.now()).build());
        return ProductResponse.from(saved);
    }

    public List<StockMovementResponse> history(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new com.pcforge.catalogo.exception.ResourceNotFoundException("No existe un producto con el id " + productId);
        }
        return stockMovementRepository.findByProductIdOrderByOccurredAtDesc(productId).stream().map(StockMovementResponse::from).toList();
    }

    private String normalizeReason(StockAdjustmentRequest request) {
        if (request.reason() != null && !request.reason().isBlank()) {
            return request.reason().trim();
        }
        return switch (request.movementType()) {
            case ENTRADA -> "Reposicion de inventario";
            case SALIDA -> "Salida de inventario";
            case AJUSTE -> "Ajuste de inventario";
        };
    }

    public StockService(final ProductRepository productRepository, final StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }
}
