package com.pcforge.catalogo.domain;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Registro de un cambio de existencia. Se guarda uno por cada movimiento
 * para poder explicar despues por que un producto quedo en determinado stock.
 */
@Document(collection = "stockMovements")
public class StockMovement {
    @Id
    private String id;
    @NotNull
    private String productId;
    @NotNull
    private MovementType movementType;
    /**
     * Cantidad positiva; el tipo de movimiento indica el sentido del cambio.
     */
    @Min(value = 1, message = "La cantidad del movimiento debe ser mayor que cero")
    private int quantity;
    private String reason;
    /**
     * Identificador del usuario autenticado que provoco el movimiento.
     */
    private String actorId;
    private Instant occurredAt;

    public static class StockMovementBuilder {

        private String id;

        private String productId;

        private MovementType movementType;

        private int quantity;

        private String reason;

        private String actorId;

        private Instant occurredAt;

        StockMovementBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public StockMovement.StockMovementBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public StockMovement.StockMovementBuilder productId(final String productId) {
            this.productId = productId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public StockMovement.StockMovementBuilder movementType(final MovementType movementType) {
            this.movementType = movementType;
            return this;
        }

        /**
         * Cantidad positiva; el tipo de movimiento indica el sentido del cambio.
         * @return {@code this}.
         */

        public StockMovement.StockMovementBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public StockMovement.StockMovementBuilder reason(final String reason) {
            this.reason = reason;
            return this;
        }

        /**
         * Identificador del usuario autenticado que provoco el movimiento.
         * @return {@code this}.
         */

        public StockMovement.StockMovementBuilder actorId(final String actorId) {
            this.actorId = actorId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public StockMovement.StockMovementBuilder occurredAt(final Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public StockMovement build() {
            return new StockMovement(this.id, this.productId, this.movementType, this.quantity, this.reason, this.actorId, this.occurredAt);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "StockMovement.StockMovementBuilder(id=" + this.id + ", productId=" + this.productId + ", movementType=" + this.movementType + ", quantity=" + this.quantity + ", reason=" + this.reason + ", actorId=" + this.actorId + ", occurredAt=" + this.occurredAt + ")";
        }
    }

    public static StockMovement.StockMovementBuilder builder() {
        return new StockMovement.StockMovementBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getProductId() {
        return this.productId;
    }

    public MovementType getMovementType() {
        return this.movementType;
    }

    /**
     * Cantidad positiva; el tipo de movimiento indica el sentido del cambio.
     */

    public int getQuantity() {
        return this.quantity;
    }

    public String getReason() {
        return this.reason;
    }

    /**
     * Identificador del usuario autenticado que provoco el movimiento.
     */

    public String getActorId() {
        return this.actorId;
    }

    public Instant getOccurredAt() {
        return this.occurredAt;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setProductId(final String productId) {
        this.productId = productId;
    }

    public void setMovementType(final MovementType movementType) {
        this.movementType = movementType;
    }

    /**
     * Cantidad positiva; el tipo de movimiento indica el sentido del cambio.
     */

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    /**
     * Identificador del usuario autenticado que provoco el movimiento.
     */

    public void setActorId(final String actorId) {
        this.actorId = actorId;
    }

    public void setOccurredAt(final Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof StockMovement)) return false;
        final StockMovement other = (StockMovement) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.getQuantity() != other.getQuantity()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$productId = this.getProductId();
        final java.lang.Object other$productId = other.getProductId();
        if (this$productId == null ? other$productId != null : !this$productId.equals(other$productId)) return false;
        final java.lang.Object this$movementType = this.getMovementType();
        final java.lang.Object other$movementType = other.getMovementType();
        if (this$movementType == null ? other$movementType != null : !this$movementType.equals(other$movementType)) return false;
        final java.lang.Object this$reason = this.getReason();
        final java.lang.Object other$reason = other.getReason();
        if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
        final java.lang.Object this$actorId = this.getActorId();
        final java.lang.Object other$actorId = other.getActorId();
        if (this$actorId == null ? other$actorId != null : !this$actorId.equals(other$actorId)) return false;
        final java.lang.Object this$occurredAt = this.getOccurredAt();
        final java.lang.Object other$occurredAt = other.getOccurredAt();
        if (this$occurredAt == null ? other$occurredAt != null : !this$occurredAt.equals(other$occurredAt)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof StockMovement;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getQuantity();
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $productId = this.getProductId();
        result = result * PRIME + ($productId == null ? 43 : $productId.hashCode());
        final java.lang.Object $movementType = this.getMovementType();
        result = result * PRIME + ($movementType == null ? 43 : $movementType.hashCode());
        final java.lang.Object $reason = this.getReason();
        result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
        final java.lang.Object $actorId = this.getActorId();
        result = result * PRIME + ($actorId == null ? 43 : $actorId.hashCode());
        final java.lang.Object $occurredAt = this.getOccurredAt();
        result = result * PRIME + ($occurredAt == null ? 43 : $occurredAt.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "StockMovement(id=" + this.getId() + ", productId=" + this.getProductId() + ", movementType=" + this.getMovementType() + ", quantity=" + this.getQuantity() + ", reason=" + this.getReason() + ", actorId=" + this.getActorId() + ", occurredAt=" + this.getOccurredAt() + ")";
    }

    public StockMovement() {
    }

    /**
     * Creates a new {@code StockMovement} instance.
     *
     * @param id
     * @param productId
     * @param movementType
     * @param quantity Cantidad positiva; el tipo de movimiento indica el sentido del cambio.
     * @param reason
     * @param actorId Identificador del usuario autenticado que provoco el movimiento.
     * @param occurredAt
     */

    public StockMovement(final String id, final String productId, final MovementType movementType, final int quantity, final String reason, final String actorId, final Instant occurredAt) {
        this.id = id;
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.reason = reason;
        this.actorId = actorId;
        this.occurredAt = occurredAt;
    }
}
