package com.pcforge.pedidos.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cotizacion o pedido del cliente.
 *
 * Guarda una copia de los componentes y de los precios vigentes en el momento
 * de la solicitud: si el catalogo cambia despues, lo que se cotizo sigue
 * siendo verificable. Esa copia es la razon por la que este servicio tiene su
 * propia base y no consulta la del catalogo al mostrar un pedido antiguo.
 */
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    /**
     * Numero legible para el cliente, por ejemplo PF-000007.
     */
    @Indexed(unique = true)
    private String orderNumber;
    private String customerId;
    private String customerEmail;
    private String customerName;
    /**
     * Configuracion de origen en el microservicio de compatibilidad.
     */
    private String configurationId;
    /**
     * Resultado de compatibilidad que tenia la configuracion al solicitarse.
     */
    private String compatibilityResult;
    private List<OrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal total;
    /**
     * Observacion inicial del cliente.
     */
    private String customerNote;
    private OrderStatus status;
    /**
     * Tecnico asignado al ensamblaje, cuando ya se asigno.
     */
    private String assignedTechnicianId;
    private String assignedTechnicianName;
    private List<Observation> observations;
    private List<StatusChange> history;
    private boolean stockDiscounted;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    private Instant deliveredAt;

    // ------------------------------------------------------------ anidados
    /**
     * Componente cotizado, con el precio que tenia al momento de la solicitud.
     */
    public static class OrderItem {
        private String productId;
        private String sku;
        private String name;
        private String componentType;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal lineTotal;

        public static class OrderItemBuilder {

            private String productId;

            private String sku;

            private String name;

            private String componentType;

            private BigDecimal unitPrice;

            private int quantity;

            private BigDecimal lineTotal;

            OrderItemBuilder() {
            }

            /**
             * @return {@code this}.
             */

            public Order.OrderItem.OrderItemBuilder productId(final String productId) {
                this.productId = productId;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.OrderItem.OrderItemBuilder sku(final String sku) {
                this.sku = sku;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.OrderItem.OrderItemBuilder name(final String name) {
                this.name = name;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.OrderItem.OrderItemBuilder componentType(final String componentType) {
                this.componentType = componentType;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.OrderItem.OrderItemBuilder unitPrice(final BigDecimal unitPrice) {
                this.unitPrice = unitPrice;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.OrderItem.OrderItemBuilder quantity(final int quantity) {
                this.quantity = quantity;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.OrderItem.OrderItemBuilder lineTotal(final BigDecimal lineTotal) {
                this.lineTotal = lineTotal;
                return this;
            }

            public Order.OrderItem build() {
                return new Order.OrderItem(this.productId, this.sku, this.name, this.componentType, this.unitPrice, this.quantity, this.lineTotal);
            }

            @java.lang.Override

            public java.lang.String toString() {
                return "Order.OrderItem.OrderItemBuilder(productId=" + this.productId + ", sku=" + this.sku + ", name=" + this.name + ", componentType=" + this.componentType + ", unitPrice=" + this.unitPrice + ", quantity=" + this.quantity + ", lineTotal=" + this.lineTotal + ")";
            }
        }

        public static Order.OrderItem.OrderItemBuilder builder() {
            return new Order.OrderItem.OrderItemBuilder();
        }

        public String getProductId() {
            return this.productId;
        }

        public String getSku() {
            return this.sku;
        }

        public String getName() {
            return this.name;
        }

        public String getComponentType() {
            return this.componentType;
        }

        public BigDecimal getUnitPrice() {
            return this.unitPrice;
        }

        public int getQuantity() {
            return this.quantity;
        }

        public BigDecimal getLineTotal() {
            return this.lineTotal;
        }

        public void setProductId(final String productId) {
            this.productId = productId;
        }

        public void setSku(final String sku) {
            this.sku = sku;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setComponentType(final String componentType) {
            this.componentType = componentType;
        }

        public void setUnitPrice(final BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public void setQuantity(final int quantity) {
            this.quantity = quantity;
        }

        public void setLineTotal(final BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }

        @java.lang.Override

        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Order.OrderItem)) return false;
            final Order.OrderItem other = (Order.OrderItem) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            if (this.getQuantity() != other.getQuantity()) return false;
            final java.lang.Object this$productId = this.getProductId();
            final java.lang.Object other$productId = other.getProductId();
            if (this$productId == null ? other$productId != null : !this$productId.equals(other$productId)) return false;
            final java.lang.Object this$sku = this.getSku();
            final java.lang.Object other$sku = other.getSku();
            if (this$sku == null ? other$sku != null : !this$sku.equals(other$sku)) return false;
            final java.lang.Object this$name = this.getName();
            final java.lang.Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            final java.lang.Object this$componentType = this.getComponentType();
            final java.lang.Object other$componentType = other.getComponentType();
            if (this$componentType == null ? other$componentType != null : !this$componentType.equals(other$componentType)) return false;
            final java.lang.Object this$unitPrice = this.getUnitPrice();
            final java.lang.Object other$unitPrice = other.getUnitPrice();
            if (this$unitPrice == null ? other$unitPrice != null : !this$unitPrice.equals(other$unitPrice)) return false;
            final java.lang.Object this$lineTotal = this.getLineTotal();
            final java.lang.Object other$lineTotal = other.getLineTotal();
            if (this$lineTotal == null ? other$lineTotal != null : !this$lineTotal.equals(other$lineTotal)) return false;
            return true;
        }

        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof Order.OrderItem;
        }

        @java.lang.Override

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getQuantity();
            final java.lang.Object $productId = this.getProductId();
            result = result * PRIME + ($productId == null ? 43 : $productId.hashCode());
            final java.lang.Object $sku = this.getSku();
            result = result * PRIME + ($sku == null ? 43 : $sku.hashCode());
            final java.lang.Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            final java.lang.Object $componentType = this.getComponentType();
            result = result * PRIME + ($componentType == null ? 43 : $componentType.hashCode());
            final java.lang.Object $unitPrice = this.getUnitPrice();
            result = result * PRIME + ($unitPrice == null ? 43 : $unitPrice.hashCode());
            final java.lang.Object $lineTotal = this.getLineTotal();
            result = result * PRIME + ($lineTotal == null ? 43 : $lineTotal.hashCode());
            return result;
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "Order.OrderItem(productId=" + this.getProductId() + ", sku=" + this.getSku() + ", name=" + this.getName() + ", componentType=" + this.getComponentType() + ", unitPrice=" + this.getUnitPrice() + ", quantity=" + this.getQuantity() + ", lineTotal=" + this.getLineTotal() + ")";
        }

        public OrderItem() {
        }

        public OrderItem(final String productId, final String sku, final String name, final String componentType, final BigDecimal unitPrice, final int quantity, final BigDecimal lineTotal) {
            this.productId = productId;
            this.sku = sku;
            this.name = name;
            this.componentType = componentType;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.lineTotal = lineTotal;
        }
    }

    /**
     * Comentario de seguimiento, del asesor o del tecnico.
     */
    public static class Observation {
        private String authorId;
        private String authorName;
        private String authorRole;
        private String text;
        private Instant createdAt;

        public static class ObservationBuilder {

            private String authorId;

            private String authorName;

            private String authorRole;

            private String text;

            private Instant createdAt;

            ObservationBuilder() {
            }

            /**
             * @return {@code this}.
             */

            public Order.Observation.ObservationBuilder authorId(final String authorId) {
                this.authorId = authorId;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.Observation.ObservationBuilder authorName(final String authorName) {
                this.authorName = authorName;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.Observation.ObservationBuilder authorRole(final String authorRole) {
                this.authorRole = authorRole;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.Observation.ObservationBuilder text(final String text) {
                this.text = text;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.Observation.ObservationBuilder createdAt(final Instant createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Order.Observation build() {
                return new Order.Observation(this.authorId, this.authorName, this.authorRole, this.text, this.createdAt);
            }

            @java.lang.Override

            public java.lang.String toString() {
                return "Order.Observation.ObservationBuilder(authorId=" + this.authorId + ", authorName=" + this.authorName + ", authorRole=" + this.authorRole + ", text=" + this.text + ", createdAt=" + this.createdAt + ")";
            }
        }

        public static Order.Observation.ObservationBuilder builder() {
            return new Order.Observation.ObservationBuilder();
        }

        public String getAuthorId() {
            return this.authorId;
        }

        public String getAuthorName() {
            return this.authorName;
        }

        public String getAuthorRole() {
            return this.authorRole;
        }

        public String getText() {
            return this.text;
        }

        public Instant getCreatedAt() {
            return this.createdAt;
        }

        public void setAuthorId(final String authorId) {
            this.authorId = authorId;
        }

        public void setAuthorName(final String authorName) {
            this.authorName = authorName;
        }

        public void setAuthorRole(final String authorRole) {
            this.authorRole = authorRole;
        }

        public void setText(final String text) {
            this.text = text;
        }

        public void setCreatedAt(final Instant createdAt) {
            this.createdAt = createdAt;
        }

        @java.lang.Override

        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Order.Observation)) return false;
            final Order.Observation other = (Order.Observation) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$authorId = this.getAuthorId();
            final java.lang.Object other$authorId = other.getAuthorId();
            if (this$authorId == null ? other$authorId != null : !this$authorId.equals(other$authorId)) return false;
            final java.lang.Object this$authorName = this.getAuthorName();
            final java.lang.Object other$authorName = other.getAuthorName();
            if (this$authorName == null ? other$authorName != null : !this$authorName.equals(other$authorName)) return false;
            final java.lang.Object this$authorRole = this.getAuthorRole();
            final java.lang.Object other$authorRole = other.getAuthorRole();
            if (this$authorRole == null ? other$authorRole != null : !this$authorRole.equals(other$authorRole)) return false;
            final java.lang.Object this$text = this.getText();
            final java.lang.Object other$text = other.getText();
            if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
            final java.lang.Object this$createdAt = this.getCreatedAt();
            final java.lang.Object other$createdAt = other.getCreatedAt();
            if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
            return true;
        }

        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof Order.Observation;
        }

        @java.lang.Override

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $authorId = this.getAuthorId();
            result = result * PRIME + ($authorId == null ? 43 : $authorId.hashCode());
            final java.lang.Object $authorName = this.getAuthorName();
            result = result * PRIME + ($authorName == null ? 43 : $authorName.hashCode());
            final java.lang.Object $authorRole = this.getAuthorRole();
            result = result * PRIME + ($authorRole == null ? 43 : $authorRole.hashCode());
            final java.lang.Object $text = this.getText();
            result = result * PRIME + ($text == null ? 43 : $text.hashCode());
            final java.lang.Object $createdAt = this.getCreatedAt();
            result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
            return result;
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "Order.Observation(authorId=" + this.getAuthorId() + ", authorName=" + this.getAuthorName() + ", authorRole=" + this.getAuthorRole() + ", text=" + this.getText() + ", createdAt=" + this.getCreatedAt() + ")";
        }

        public Observation() {
        }

        public Observation(final String authorId, final String authorName, final String authorRole, final String text, final Instant createdAt) {
            this.authorId = authorId;
            this.authorName = authorName;
            this.authorRole = authorRole;
            this.text = text;
            this.createdAt = createdAt;
        }
    }

    /**
     * Registro de un cambio de estado. Nunca se sobrescribe.
     */
    public static class StatusChange {
        private OrderStatus from;
        private OrderStatus to;
        private String changedById;
        private String changedByName;
        private String note;
        private Instant changedAt;

        public static class StatusChangeBuilder {

            private OrderStatus from;

            private OrderStatus to;

            private String changedById;

            private String changedByName;

            private String note;

            private Instant changedAt;

            StatusChangeBuilder() {
            }

            /**
             * @return {@code this}.
             */

            public Order.StatusChange.StatusChangeBuilder from(final OrderStatus from) {
                this.from = from;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.StatusChange.StatusChangeBuilder to(final OrderStatus to) {
                this.to = to;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.StatusChange.StatusChangeBuilder changedById(final String changedById) {
                this.changedById = changedById;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.StatusChange.StatusChangeBuilder changedByName(final String changedByName) {
                this.changedByName = changedByName;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.StatusChange.StatusChangeBuilder note(final String note) {
                this.note = note;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public Order.StatusChange.StatusChangeBuilder changedAt(final Instant changedAt) {
                this.changedAt = changedAt;
                return this;
            }

            public Order.StatusChange build() {
                return new Order.StatusChange(this.from, this.to, this.changedById, this.changedByName, this.note, this.changedAt);
            }

            @java.lang.Override

            public java.lang.String toString() {
                return "Order.StatusChange.StatusChangeBuilder(from=" + this.from + ", to=" + this.to + ", changedById=" + this.changedById + ", changedByName=" + this.changedByName + ", note=" + this.note + ", changedAt=" + this.changedAt + ")";
            }
        }

        public static Order.StatusChange.StatusChangeBuilder builder() {
            return new Order.StatusChange.StatusChangeBuilder();
        }

        public OrderStatus getFrom() {
            return this.from;
        }

        public OrderStatus getTo() {
            return this.to;
        }

        public String getChangedById() {
            return this.changedById;
        }

        public String getChangedByName() {
            return this.changedByName;
        }

        public String getNote() {
            return this.note;
        }

        public Instant getChangedAt() {
            return this.changedAt;
        }

        public void setFrom(final OrderStatus from) {
            this.from = from;
        }

        public void setTo(final OrderStatus to) {
            this.to = to;
        }

        public void setChangedById(final String changedById) {
            this.changedById = changedById;
        }

        public void setChangedByName(final String changedByName) {
            this.changedByName = changedByName;
        }

        public void setNote(final String note) {
            this.note = note;
        }

        public void setChangedAt(final Instant changedAt) {
            this.changedAt = changedAt;
        }

        @java.lang.Override

        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Order.StatusChange)) return false;
            final Order.StatusChange other = (Order.StatusChange) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$from = this.getFrom();
            final java.lang.Object other$from = other.getFrom();
            if (this$from == null ? other$from != null : !this$from.equals(other$from)) return false;
            final java.lang.Object this$to = this.getTo();
            final java.lang.Object other$to = other.getTo();
            if (this$to == null ? other$to != null : !this$to.equals(other$to)) return false;
            final java.lang.Object this$changedById = this.getChangedById();
            final java.lang.Object other$changedById = other.getChangedById();
            if (this$changedById == null ? other$changedById != null : !this$changedById.equals(other$changedById)) return false;
            final java.lang.Object this$changedByName = this.getChangedByName();
            final java.lang.Object other$changedByName = other.getChangedByName();
            if (this$changedByName == null ? other$changedByName != null : !this$changedByName.equals(other$changedByName)) return false;
            final java.lang.Object this$note = this.getNote();
            final java.lang.Object other$note = other.getNote();
            if (this$note == null ? other$note != null : !this$note.equals(other$note)) return false;
            final java.lang.Object this$changedAt = this.getChangedAt();
            final java.lang.Object other$changedAt = other.getChangedAt();
            if (this$changedAt == null ? other$changedAt != null : !this$changedAt.equals(other$changedAt)) return false;
            return true;
        }

        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof Order.StatusChange;
        }

        @java.lang.Override

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $from = this.getFrom();
            result = result * PRIME + ($from == null ? 43 : $from.hashCode());
            final java.lang.Object $to = this.getTo();
            result = result * PRIME + ($to == null ? 43 : $to.hashCode());
            final java.lang.Object $changedById = this.getChangedById();
            result = result * PRIME + ($changedById == null ? 43 : $changedById.hashCode());
            final java.lang.Object $changedByName = this.getChangedByName();
            result = result * PRIME + ($changedByName == null ? 43 : $changedByName.hashCode());
            final java.lang.Object $note = this.getNote();
            result = result * PRIME + ($note == null ? 43 : $note.hashCode());
            final java.lang.Object $changedAt = this.getChangedAt();
            result = result * PRIME + ($changedAt == null ? 43 : $changedAt.hashCode());
            return result;
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "Order.StatusChange(from=" + this.getFrom() + ", to=" + this.getTo() + ", changedById=" + this.getChangedById() + ", changedByName=" + this.getChangedByName() + ", note=" + this.getNote() + ", changedAt=" + this.getChangedAt() + ")";
        }

        public StatusChange() {
        }

        public StatusChange(final OrderStatus from, final OrderStatus to, final String changedById, final String changedByName, final String note, final Instant changedAt) {
            this.from = from;
            this.to = to;
            this.changedById = changedById;
            this.changedByName = changedByName;
            this.note = note;
            this.changedAt = changedAt;
        }
    }

    private static List<OrderItem> $default$items() {
        return new ArrayList<>();
    }

    private static BigDecimal $default$subtotal() {
        return BigDecimal.ZERO;
    }

    private static BigDecimal $default$total() {
        return BigDecimal.ZERO;
    }

    private static OrderStatus $default$status() {
        return OrderStatus.RECIBIDO;
    }

    private static List<Observation> $default$observations() {
        return new ArrayList<>();
    }

    private static List<StatusChange> $default$history() {
        return new ArrayList<>();
    }

    private static boolean $default$stockDiscounted() {
        return false;
    }

    public static class OrderBuilder {

        private String id;

        private String orderNumber;

        private String customerId;

        private String customerEmail;

        private String customerName;

        private String configurationId;

        private String compatibilityResult;

        private boolean items$set;

        private List<OrderItem> items$value;

        private boolean subtotal$set;

        private BigDecimal subtotal$value;

        private boolean total$set;

        private BigDecimal total$value;

        private String customerNote;

        private boolean status$set;

        private OrderStatus status$value;

        private String assignedTechnicianId;

        private String assignedTechnicianName;

        private boolean observations$set;

        private List<Observation> observations$value;

        private boolean history$set;

        private List<StatusChange> history$value;

        private boolean stockDiscounted$set;

        private boolean stockDiscounted$value;

        private Instant createdAt;

        private Instant updatedAt;

        private Instant deliveredAt;

        OrderBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * Numero legible para el cliente, por ejemplo PF-000007.
         * @return {@code this}.
         */

        public Order.OrderBuilder orderNumber(final String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder customerId(final String customerId) {
            this.customerId = customerId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder customerEmail(final String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder customerName(final String customerName) {
            this.customerName = customerName;
            return this;
        }

        /**
         * Configuracion de origen en el microservicio de compatibilidad.
         * @return {@code this}.
         */

        public Order.OrderBuilder configurationId(final String configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        /**
         * Resultado de compatibilidad que tenia la configuracion al solicitarse.
         * @return {@code this}.
         */

        public Order.OrderBuilder compatibilityResult(final String compatibilityResult) {
            this.compatibilityResult = compatibilityResult;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder items(final List<OrderItem> items) {
            this.items$value = items;
            items$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder subtotal(final BigDecimal subtotal) {
            this.subtotal$value = subtotal;
            subtotal$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder total(final BigDecimal total) {
            this.total$value = total;
            total$set = true;
            return this;
        }

        /**
         * Observacion inicial del cliente.
         * @return {@code this}.
         */

        public Order.OrderBuilder customerNote(final String customerNote) {
            this.customerNote = customerNote;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder status(final OrderStatus status) {
            this.status$value = status;
            status$set = true;
            return this;
        }

        /**
         * Tecnico asignado al ensamblaje, cuando ya se asigno.
         * @return {@code this}.
         */

        public Order.OrderBuilder assignedTechnicianId(final String assignedTechnicianId) {
            this.assignedTechnicianId = assignedTechnicianId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder assignedTechnicianName(final String assignedTechnicianName) {
            this.assignedTechnicianName = assignedTechnicianName;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder observations(final List<Observation> observations) {
            this.observations$value = observations;
            observations$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder history(final List<StatusChange> history) {
            this.history$value = history;
            history$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder stockDiscounted(final boolean stockDiscounted) {
            this.stockDiscounted$value = stockDiscounted;
            stockDiscounted$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Order.OrderBuilder deliveredAt(final Instant deliveredAt) {
            this.deliveredAt = deliveredAt;
            return this;
        }

        public Order build() {
            List<OrderItem> items$value = this.items$value;
            if (!this.items$set) items$value = Order.$default$items();
            BigDecimal subtotal$value = this.subtotal$value;
            if (!this.subtotal$set) subtotal$value = Order.$default$subtotal();
            BigDecimal total$value = this.total$value;
            if (!this.total$set) total$value = Order.$default$total();
            OrderStatus status$value = this.status$value;
            if (!this.status$set) status$value = Order.$default$status();
            List<Observation> observations$value = this.observations$value;
            if (!this.observations$set) observations$value = Order.$default$observations();
            List<StatusChange> history$value = this.history$value;
            if (!this.history$set) history$value = Order.$default$history();
            boolean stockDiscounted$value = this.stockDiscounted$value;
            if (!this.stockDiscounted$set) stockDiscounted$value = Order.$default$stockDiscounted();
            return new Order(this.id, this.orderNumber, this.customerId, this.customerEmail, this.customerName, this.configurationId, this.compatibilityResult, items$value, subtotal$value, total$value, this.customerNote, status$value, this.assignedTechnicianId, this.assignedTechnicianName, observations$value, history$value, stockDiscounted$value, this.createdAt, this.updatedAt, this.deliveredAt);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "Order.OrderBuilder(id=" + this.id + ", orderNumber=" + this.orderNumber + ", customerId=" + this.customerId + ", customerEmail=" + this.customerEmail + ", customerName=" + this.customerName + ", configurationId=" + this.configurationId + ", compatibilityResult=" + this.compatibilityResult + ", items$value=" + this.items$value + ", subtotal$value=" + this.subtotal$value + ", total$value=" + this.total$value + ", customerNote=" + this.customerNote + ", status$value=" + this.status$value + ", assignedTechnicianId=" + this.assignedTechnicianId + ", assignedTechnicianName=" + this.assignedTechnicianName + ", observations$value=" + this.observations$value + ", history$value=" + this.history$value + ", stockDiscounted$value=" + this.stockDiscounted$value + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", deliveredAt=" + this.deliveredAt + ")";
        }
    }

    public static Order.OrderBuilder builder() {
        return new Order.OrderBuilder();
    }

    public String getId() {
        return this.id;
    }

    /**
     * Numero legible para el cliente, por ejemplo PF-000007.
     */

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public String getCustomerEmail() {
        return this.customerEmail;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    /**
     * Configuracion de origen en el microservicio de compatibilidad.
     */

    public String getConfigurationId() {
        return this.configurationId;
    }

    /**
     * Resultado de compatibilidad que tenia la configuracion al solicitarse.
     */

    public String getCompatibilityResult() {
        return this.compatibilityResult;
    }

    public List<OrderItem> getItems() {
        return this.items;
    }

    public BigDecimal getSubtotal() {
        return this.subtotal;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    /**
     * Observacion inicial del cliente.
     */

    public String getCustomerNote() {
        return this.customerNote;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    /**
     * Tecnico asignado al ensamblaje, cuando ya se asigno.
     */

    public String getAssignedTechnicianId() {
        return this.assignedTechnicianId;
    }

    public String getAssignedTechnicianName() {
        return this.assignedTechnicianName;
    }

    public List<Observation> getObservations() {
        return this.observations;
    }

    public List<StatusChange> getHistory() {
        return this.history;
    }

    public boolean isStockDiscounted() {
        return this.stockDiscounted;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Instant getDeliveredAt() {
        return this.deliveredAt;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Numero legible para el cliente, por ejemplo PF-000007.
     */

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerEmail(final String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }

    /**
     * Configuracion de origen en el microservicio de compatibilidad.
     */

    public void setConfigurationId(final String configurationId) {
        this.configurationId = configurationId;
    }

    /**
     * Resultado de compatibilidad que tenia la configuracion al solicitarse.
     */

    public void setCompatibilityResult(final String compatibilityResult) {
        this.compatibilityResult = compatibilityResult;
    }

    public void setItems(final List<OrderItem> items) {
        this.items = items;
    }

    public void setSubtotal(final BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setTotal(final BigDecimal total) {
        this.total = total;
    }

    /**
     * Observacion inicial del cliente.
     */

    public void setCustomerNote(final String customerNote) {
        this.customerNote = customerNote;
    }

    public void setStatus(final OrderStatus status) {
        this.status = status;
    }

    /**
     * Tecnico asignado al ensamblaje, cuando ya se asigno.
     */

    public void setAssignedTechnicianId(final String assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public void setAssignedTechnicianName(final String assignedTechnicianName) {
        this.assignedTechnicianName = assignedTechnicianName;
    }

    public void setObservations(final List<Observation> observations) {
        this.observations = observations;
    }

    public void setHistory(final List<StatusChange> history) {
        this.history = history;
    }

    public void setStockDiscounted(final boolean stockDiscounted) {
        this.stockDiscounted = stockDiscounted;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeliveredAt(final Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Order)) return false;
        final Order other = (Order) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.isStockDiscounted() != other.isStockDiscounted()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$orderNumber = this.getOrderNumber();
        final java.lang.Object other$orderNumber = other.getOrderNumber();
        if (this$orderNumber == null ? other$orderNumber != null : !this$orderNumber.equals(other$orderNumber)) return false;
        final java.lang.Object this$customerId = this.getCustomerId();
        final java.lang.Object other$customerId = other.getCustomerId();
        if (this$customerId == null ? other$customerId != null : !this$customerId.equals(other$customerId)) return false;
        final java.lang.Object this$customerEmail = this.getCustomerEmail();
        final java.lang.Object other$customerEmail = other.getCustomerEmail();
        if (this$customerEmail == null ? other$customerEmail != null : !this$customerEmail.equals(other$customerEmail)) return false;
        final java.lang.Object this$customerName = this.getCustomerName();
        final java.lang.Object other$customerName = other.getCustomerName();
        if (this$customerName == null ? other$customerName != null : !this$customerName.equals(other$customerName)) return false;
        final java.lang.Object this$configurationId = this.getConfigurationId();
        final java.lang.Object other$configurationId = other.getConfigurationId();
        if (this$configurationId == null ? other$configurationId != null : !this$configurationId.equals(other$configurationId)) return false;
        final java.lang.Object this$compatibilityResult = this.getCompatibilityResult();
        final java.lang.Object other$compatibilityResult = other.getCompatibilityResult();
        if (this$compatibilityResult == null ? other$compatibilityResult != null : !this$compatibilityResult.equals(other$compatibilityResult)) return false;
        final java.lang.Object this$items = this.getItems();
        final java.lang.Object other$items = other.getItems();
        if (this$items == null ? other$items != null : !this$items.equals(other$items)) return false;
        final java.lang.Object this$subtotal = this.getSubtotal();
        final java.lang.Object other$subtotal = other.getSubtotal();
        if (this$subtotal == null ? other$subtotal != null : !this$subtotal.equals(other$subtotal)) return false;
        final java.lang.Object this$total = this.getTotal();
        final java.lang.Object other$total = other.getTotal();
        if (this$total == null ? other$total != null : !this$total.equals(other$total)) return false;
        final java.lang.Object this$customerNote = this.getCustomerNote();
        final java.lang.Object other$customerNote = other.getCustomerNote();
        if (this$customerNote == null ? other$customerNote != null : !this$customerNote.equals(other$customerNote)) return false;
        final java.lang.Object this$status = this.getStatus();
        final java.lang.Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final java.lang.Object this$assignedTechnicianId = this.getAssignedTechnicianId();
        final java.lang.Object other$assignedTechnicianId = other.getAssignedTechnicianId();
        if (this$assignedTechnicianId == null ? other$assignedTechnicianId != null : !this$assignedTechnicianId.equals(other$assignedTechnicianId)) return false;
        final java.lang.Object this$assignedTechnicianName = this.getAssignedTechnicianName();
        final java.lang.Object other$assignedTechnicianName = other.getAssignedTechnicianName();
        if (this$assignedTechnicianName == null ? other$assignedTechnicianName != null : !this$assignedTechnicianName.equals(other$assignedTechnicianName)) return false;
        final java.lang.Object this$observations = this.getObservations();
        final java.lang.Object other$observations = other.getObservations();
        if (this$observations == null ? other$observations != null : !this$observations.equals(other$observations)) return false;
        final java.lang.Object this$history = this.getHistory();
        final java.lang.Object other$history = other.getHistory();
        if (this$history == null ? other$history != null : !this$history.equals(other$history)) return false;
        final java.lang.Object this$createdAt = this.getCreatedAt();
        final java.lang.Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        final java.lang.Object this$updatedAt = this.getUpdatedAt();
        final java.lang.Object other$updatedAt = other.getUpdatedAt();
        if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
        final java.lang.Object this$deliveredAt = this.getDeliveredAt();
        final java.lang.Object other$deliveredAt = other.getDeliveredAt();
        if (this$deliveredAt == null ? other$deliveredAt != null : !this$deliveredAt.equals(other$deliveredAt)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Order;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isStockDiscounted() ? 79 : 97);
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $orderNumber = this.getOrderNumber();
        result = result * PRIME + ($orderNumber == null ? 43 : $orderNumber.hashCode());
        final java.lang.Object $customerId = this.getCustomerId();
        result = result * PRIME + ($customerId == null ? 43 : $customerId.hashCode());
        final java.lang.Object $customerEmail = this.getCustomerEmail();
        result = result * PRIME + ($customerEmail == null ? 43 : $customerEmail.hashCode());
        final java.lang.Object $customerName = this.getCustomerName();
        result = result * PRIME + ($customerName == null ? 43 : $customerName.hashCode());
        final java.lang.Object $configurationId = this.getConfigurationId();
        result = result * PRIME + ($configurationId == null ? 43 : $configurationId.hashCode());
        final java.lang.Object $compatibilityResult = this.getCompatibilityResult();
        result = result * PRIME + ($compatibilityResult == null ? 43 : $compatibilityResult.hashCode());
        final java.lang.Object $items = this.getItems();
        result = result * PRIME + ($items == null ? 43 : $items.hashCode());
        final java.lang.Object $subtotal = this.getSubtotal();
        result = result * PRIME + ($subtotal == null ? 43 : $subtotal.hashCode());
        final java.lang.Object $total = this.getTotal();
        result = result * PRIME + ($total == null ? 43 : $total.hashCode());
        final java.lang.Object $customerNote = this.getCustomerNote();
        result = result * PRIME + ($customerNote == null ? 43 : $customerNote.hashCode());
        final java.lang.Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final java.lang.Object $assignedTechnicianId = this.getAssignedTechnicianId();
        result = result * PRIME + ($assignedTechnicianId == null ? 43 : $assignedTechnicianId.hashCode());
        final java.lang.Object $assignedTechnicianName = this.getAssignedTechnicianName();
        result = result * PRIME + ($assignedTechnicianName == null ? 43 : $assignedTechnicianName.hashCode());
        final java.lang.Object $observations = this.getObservations();
        result = result * PRIME + ($observations == null ? 43 : $observations.hashCode());
        final java.lang.Object $history = this.getHistory();
        result = result * PRIME + ($history == null ? 43 : $history.hashCode());
        final java.lang.Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        final java.lang.Object $updatedAt = this.getUpdatedAt();
        result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
        final java.lang.Object $deliveredAt = this.getDeliveredAt();
        result = result * PRIME + ($deliveredAt == null ? 43 : $deliveredAt.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "Order(id=" + this.getId() + ", orderNumber=" + this.getOrderNumber() + ", customerId=" + this.getCustomerId() + ", customerEmail=" + this.getCustomerEmail() + ", customerName=" + this.getCustomerName() + ", configurationId=" + this.getConfigurationId() + ", compatibilityResult=" + this.getCompatibilityResult() + ", items=" + this.getItems() + ", subtotal=" + this.getSubtotal() + ", total=" + this.getTotal() + ", customerNote=" + this.getCustomerNote() + ", status=" + this.getStatus() + ", assignedTechnicianId=" + this.getAssignedTechnicianId() + ", assignedTechnicianName=" + this.getAssignedTechnicianName() + ", observations=" + this.getObservations() + ", history=" + this.getHistory() + ", stockDiscounted=" + this.isStockDiscounted() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ", deliveredAt=" + this.getDeliveredAt() + ")";
    }

    public Order() {
        this.items = Order.$default$items();
        this.subtotal = Order.$default$subtotal();
        this.total = Order.$default$total();
        this.status = Order.$default$status();
        this.observations = Order.$default$observations();
        this.history = Order.$default$history();
        this.stockDiscounted = Order.$default$stockDiscounted();
    }

    /**
     * Creates a new {@code Order} instance.
     *
     * @param id
     * @param orderNumber Numero legible para el cliente, por ejemplo PF-000007.
     * @param customerId
     * @param customerEmail
     * @param customerName
     * @param configurationId Configuracion de origen en el microservicio de compatibilidad.
     * @param compatibilityResult Resultado de compatibilidad que tenia la configuracion al solicitarse.
     * @param items
     * @param subtotal
     * @param total
     * @param customerNote Observacion inicial del cliente.
     * @param status
     * @param assignedTechnicianId Tecnico asignado al ensamblaje, cuando ya se asigno.
     * @param assignedTechnicianName
     * @param observations
     * @param history
     * @param stockDiscounted
     * @param createdAt
     * @param updatedAt
     * @param deliveredAt
     */

    public Order(final String id, final String orderNumber, final String customerId, final String customerEmail, final String customerName, final String configurationId, final String compatibilityResult, final List<OrderItem> items, final BigDecimal subtotal, final BigDecimal total, final String customerNote, final OrderStatus status, final String assignedTechnicianId, final String assignedTechnicianName, final List<Observation> observations, final List<StatusChange> history, final boolean stockDiscounted, final Instant createdAt, final Instant updatedAt, final Instant deliveredAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.configurationId = configurationId;
        this.compatibilityResult = compatibilityResult;
        this.items = items;
        this.subtotal = subtotal;
        this.total = total;
        this.customerNote = customerNote;
        this.status = status;
        this.assignedTechnicianId = assignedTechnicianId;
        this.assignedTechnicianName = assignedTechnicianName;
        this.observations = observations;
        this.history = history;
        this.stockDiscounted = stockDiscounted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deliveredAt = deliveredAt;
    }
}
