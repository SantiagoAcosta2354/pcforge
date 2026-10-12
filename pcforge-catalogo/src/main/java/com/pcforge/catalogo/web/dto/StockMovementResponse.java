package com.pcforge.catalogo.web.dto;

import java.time.Instant;

import com.pcforge.catalogo.domain.MovementType;
import com.pcforge.catalogo.domain.StockMovement;

/** Representacion de un movimiento de inventario en la API. */
public record StockMovementResponse(
        String id,
        String productId,
        MovementType movementType,
        int quantity,
        String reason,
        String actorId,
        Instant occurredAt) {

    public static StockMovementResponse from(StockMovement m) {
        return new StockMovementResponse(m.getId(), m.getProductId(), m.getMovementType(),
                m.getQuantity(), m.getReason(), m.getActorId(), m.getOccurredAt());
    }
}
