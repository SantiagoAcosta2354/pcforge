package com.pcforge.catalogo.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Producto del catalogo, con su precio, existencia y atributos tecnicos.
 * Es la fuente de verdad que consultan los servicios de compatibilidad
 * y de pedidos a traves de la API, nunca por acceso directo a la base.
 */
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    @Indexed(unique = true)
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 40)
    private String sku;
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 140)
    private String name;
    @Size(max = 80)
    private String brand;
    /**
     * Identificador de la categoria a la que pertenece el producto.
     */
    @NotBlank(message = "La categoria es obligatoria")
    private String categoryId;
    @NotNull(message = "El tipo de componente es obligatorio")
    private ComponentType componentType;
    @Size(max = 600)
    private String description;
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price;
    @Min(value = 0, message = "La existencia no puede ser negativa")
    private int stock;
    /**
     * Existencia por debajo de la cual se genera una alerta de reposicion.
     */
    private int minimumStock;
    private boolean active;
    private TechnicalSpecs specs;
    /**
     * Costo de adquisicion del producto. No se expone en la API publica: solo lo
     * consulta el perfil de precios y margenes para calcular la rentabilidad.
     */
    private BigDecimal costo;
    /**
     * Fuente y fecha de verificacion de cada atributo tecnico obligatorio.
     * La clave es el nombre del atributo (socket, ramType, capacityW...).
     */
    private Map<String, AttributeSource> attributeSources;
    /**
     * Momento en que el curador declaro la ficha completa. Si despues se retira
     * un atributo obligatorio, la ficha vuelve a incompleta y esta marca se limpia.
     */
    private Instant verifiedAt;
    private String verifiedBy;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Un producto puede ofrecerse solo si esta activo y tiene unidades.
     */
    public boolean isAvailable() {
        return active && stock > 0;
    }

    private static int $default$minimumStock() {
        return 1;
    }

    private static boolean $default$active() {
        return true;
    }

    public static class ProductBuilder {

        private String id;

        private String sku;

        private String name;

        private String brand;

        private String categoryId;

        private ComponentType componentType;

        private String description;

        private BigDecimal price;

        private int stock;

        private boolean minimumStock$set;

        private int minimumStock$value;

        private boolean active$set;

        private boolean active$value;

        private TechnicalSpecs specs;

        private Instant createdAt;

        private Instant updatedAt;

        ProductBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder sku(final String sku) {
            this.sku = sku;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder brand(final String brand) {
            this.brand = brand;
            return this;
        }

        /**
         * Identificador de la categoria a la que pertenece el producto.
         * @return {@code this}.
         */

        public Product.ProductBuilder categoryId(final String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder componentType(final ComponentType componentType) {
            this.componentType = componentType;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder price(final BigDecimal price) {
            this.price = price;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder stock(final int stock) {
            this.stock = stock;
            return this;
        }

        /**
         * Existencia por debajo de la cual se genera una alerta de reposicion.
         * @return {@code this}.
         */

        public Product.ProductBuilder minimumStock(final int minimumStock) {
            this.minimumStock$value = minimumStock;
            minimumStock$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder specs(final TechnicalSpecs specs) {
            this.specs = specs;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Product.ProductBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Product build() {
            int minimumStock$value = this.minimumStock$value;
            if (!this.minimumStock$set) minimumStock$value = Product.$default$minimumStock();
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = Product.$default$active();
            return new Product(this.id, this.sku, this.name, this.brand, this.categoryId, this.componentType, this.description, this.price, this.stock, minimumStock$value, active$value, this.specs, this.createdAt, this.updatedAt);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "Product.ProductBuilder(id=" + this.id + ", sku=" + this.sku + ", name=" + this.name + ", brand=" + this.brand + ", categoryId=" + this.categoryId + ", componentType=" + this.componentType + ", description=" + this.description + ", price=" + this.price + ", stock=" + this.stock + ", minimumStock$value=" + this.minimumStock$value + ", active$value=" + this.active$value + ", specs=" + this.specs + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ")";
        }
    }

    public static Product.ProductBuilder builder() {
        return new Product.ProductBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getSku() {
        return this.sku;
    }

    public String getName() {
        return this.name;
    }

    public String getBrand() {
        return this.brand;
    }

    /**
     * Identificador de la categoria a la que pertenece el producto.
     */

    public String getCategoryId() {
        return this.categoryId;
    }

    public ComponentType getComponentType() {
        return this.componentType;
    }

    public String getDescription() {
        return this.description;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public int getStock() {
        return this.stock;
    }

    /**
     * Existencia por debajo de la cual se genera una alerta de reposicion.
     */

    public int getMinimumStock() {
        return this.minimumStock;
    }

    public boolean isActive() {
        return this.active;
    }

    public TechnicalSpecs getSpecs() {
        return this.specs;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setSku(final String sku) {
        this.sku = sku;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    /**
     * Identificador de la categoria a la que pertenece el producto.
     */

    public void setCategoryId(final String categoryId) {
        this.categoryId = categoryId;
    }

    public void setComponentType(final ComponentType componentType) {
        this.componentType = componentType;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public void setStock(final int stock) {
        this.stock = stock;
    }

    /**
     * Existencia por debajo de la cual se genera una alerta de reposicion.
     */

    public void setMinimumStock(final int minimumStock) {
        this.minimumStock = minimumStock;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setSpecs(final TechnicalSpecs specs) {
        this.specs = specs;
    }

    /**
     * Costo de adquisicion, base del calculo de margen.
     */

    public BigDecimal getCosto() {
        return this.costo;
    }

    /**
     * Fuente y fecha de verificacion de cada atributo tecnico obligatorio.
     */

    public Map<String, AttributeSource> getAttributeSources() {
        return this.attributeSources;
    }

    public Instant getVerifiedAt() {
        return this.verifiedAt;
    }

    public String getVerifiedBy() {
        return this.verifiedBy;
    }

    public void setCosto(final BigDecimal costo) {
        this.costo = costo;
    }

    public void setAttributeSources(final Map<String, AttributeSource> attributeSources) {
        this.attributeSources = attributeSources;
    }

    public void setVerifiedAt(final Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setVerifiedBy(final String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Product)) return false;
        final Product other = (Product) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.getStock() != other.getStock()) return false;
        if (this.getMinimumStock() != other.getMinimumStock()) return false;
        if (this.isActive() != other.isActive()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$sku = this.getSku();
        final java.lang.Object other$sku = other.getSku();
        if (this$sku == null ? other$sku != null : !this$sku.equals(other$sku)) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$brand = this.getBrand();
        final java.lang.Object other$brand = other.getBrand();
        if (this$brand == null ? other$brand != null : !this$brand.equals(other$brand)) return false;
        final java.lang.Object this$categoryId = this.getCategoryId();
        final java.lang.Object other$categoryId = other.getCategoryId();
        if (this$categoryId == null ? other$categoryId != null : !this$categoryId.equals(other$categoryId)) return false;
        final java.lang.Object this$componentType = this.getComponentType();
        final java.lang.Object other$componentType = other.getComponentType();
        if (this$componentType == null ? other$componentType != null : !this$componentType.equals(other$componentType)) return false;
        final java.lang.Object this$description = this.getDescription();
        final java.lang.Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
        final java.lang.Object this$price = this.getPrice();
        final java.lang.Object other$price = other.getPrice();
        if (this$price == null ? other$price != null : !this$price.equals(other$price)) return false;
        final java.lang.Object this$specs = this.getSpecs();
        final java.lang.Object other$specs = other.getSpecs();
        if (this$specs == null ? other$specs != null : !this$specs.equals(other$specs)) return false;
        final java.lang.Object this$createdAt = this.getCreatedAt();
        final java.lang.Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        final java.lang.Object this$updatedAt = this.getUpdatedAt();
        final java.lang.Object other$updatedAt = other.getUpdatedAt();
        if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Product;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getStock();
        result = result * PRIME + this.getMinimumStock();
        result = result * PRIME + (this.isActive() ? 79 : 97);
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $sku = this.getSku();
        result = result * PRIME + ($sku == null ? 43 : $sku.hashCode());
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $brand = this.getBrand();
        result = result * PRIME + ($brand == null ? 43 : $brand.hashCode());
        final java.lang.Object $categoryId = this.getCategoryId();
        result = result * PRIME + ($categoryId == null ? 43 : $categoryId.hashCode());
        final java.lang.Object $componentType = this.getComponentType();
        result = result * PRIME + ($componentType == null ? 43 : $componentType.hashCode());
        final java.lang.Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final java.lang.Object $price = this.getPrice();
        result = result * PRIME + ($price == null ? 43 : $price.hashCode());
        final java.lang.Object $specs = this.getSpecs();
        result = result * PRIME + ($specs == null ? 43 : $specs.hashCode());
        final java.lang.Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        final java.lang.Object $updatedAt = this.getUpdatedAt();
        result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "Product(id=" + this.getId() + ", sku=" + this.getSku() + ", name=" + this.getName() + ", brand=" + this.getBrand() + ", categoryId=" + this.getCategoryId() + ", componentType=" + this.getComponentType() + ", description=" + this.getDescription() + ", price=" + this.getPrice() + ", stock=" + this.getStock() + ", minimumStock=" + this.getMinimumStock() + ", active=" + this.isActive() + ", specs=" + this.getSpecs() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
    }

    public Product() {
        this.minimumStock = Product.$default$minimumStock();
        this.active = Product.$default$active();
    }

    /**
     * Creates a new {@code Product} instance.
     *
     * @param id
     * @param sku
     * @param name
     * @param brand
     * @param categoryId Identificador de la categoria a la que pertenece el producto.
     * @param componentType
     * @param description
     * @param price
     * @param stock
     * @param minimumStock Existencia por debajo de la cual se genera una alerta de reposicion.
     * @param active
     * @param specs
     * @param createdAt
     * @param updatedAt
     */

    public Product(final String id, final String sku, final String name, final String brand, final String categoryId, final ComponentType componentType, final String description, final BigDecimal price, final int stock, final int minimumStock, final boolean active, final TechnicalSpecs specs, final Instant createdAt, final Instant updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.brand = brand;
        this.categoryId = categoryId;
        this.componentType = componentType;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.minimumStock = minimumStock;
        this.active = active;
        this.specs = specs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
