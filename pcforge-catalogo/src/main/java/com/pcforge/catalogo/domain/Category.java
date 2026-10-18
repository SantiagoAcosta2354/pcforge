package com.pcforge.catalogo.domain;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Categoria comercial del catalogo (procesadores, placas, memorias, etc.).
 */
@Document(collection = "categories")
public class Category {
    @Id
    private String id;
    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(max = 80)
    private String name;
    /**
     * Identificador legible usado en las rutas de consulta.
     */
    @Indexed(unique = true)
    @NotBlank(message = "El slug de la categoria es obligatorio")
    @Size(max = 80)
    private String slug;
    @Size(max = 300)
    private String description;
    private boolean active;
    /**
     * Margen minimo que la politica comercial exige para esta categoria,
     * expresado en porcentaje sobre el precio de venta.
     */
    private BigDecimal margenMinimo;
    /**
     * Descuento maximo que un asesor puede aplicar sin aprobacion, en porcentaje.
     */
    private BigDecimal umbralDescuento;
    /** Responsable y fecha del ultimo cambio de politica, para la trazabilidad. */
    private String policyUpdatedBy;
    private Instant policyUpdatedAt;

    private static boolean $default$active() {
        return true;
    }

    public static class CategoryBuilder {

        private String id;

        private String name;

        private String slug;

        private String description;

        private boolean active$set;

        private boolean active$value;

        CategoryBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public Category.CategoryBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Category.CategoryBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Identificador legible usado en las rutas de consulta.
         * @return {@code this}.
         */

        public Category.CategoryBuilder slug(final String slug) {
            this.slug = slug;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Category.CategoryBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Category.CategoryBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        public Category build() {
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = Category.$default$active();
            return new Category(this.id, this.name, this.slug, this.description, active$value);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "Category.CategoryBuilder(id=" + this.id + ", name=" + this.name + ", slug=" + this.slug + ", description=" + this.description + ", active$value=" + this.active$value + ")";
        }
    }

    public static Category.CategoryBuilder builder() {
        return new Category.CategoryBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Identificador legible usado en las rutas de consulta.
     */

    public String getSlug() {
        return this.slug;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Identificador legible usado en las rutas de consulta.
     */

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Margen minimo exigido por la politica comercial, en porcentaje.
     */

    public BigDecimal getMargenMinimo() {
        return this.margenMinimo;
    }

    /**
     * Descuento maximo sin aprobacion, en porcentaje.
     */

    public BigDecimal getUmbralDescuento() {
        return this.umbralDescuento;
    }

    public String getPolicyUpdatedBy() {
        return this.policyUpdatedBy;
    }

    public Instant getPolicyUpdatedAt() {
        return this.policyUpdatedAt;
    }

    public void setMargenMinimo(final BigDecimal margenMinimo) {
        this.margenMinimo = margenMinimo;
    }

    public void setUmbralDescuento(final BigDecimal umbralDescuento) {
        this.umbralDescuento = umbralDescuento;
    }

    public void setPolicyUpdatedBy(final String policyUpdatedBy) {
        this.policyUpdatedBy = policyUpdatedBy;
    }

    public void setPolicyUpdatedAt(final Instant policyUpdatedAt) {
        this.policyUpdatedAt = policyUpdatedAt;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Category)) return false;
        final Category other = (Category) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.isActive() != other.isActive()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$slug = this.getSlug();
        final java.lang.Object other$slug = other.getSlug();
        if (this$slug == null ? other$slug != null : !this$slug.equals(other$slug)) return false;
        final java.lang.Object this$description = this.getDescription();
        final java.lang.Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Category;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isActive() ? 79 : 97);
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $slug = this.getSlug();
        result = result * PRIME + ($slug == null ? 43 : $slug.hashCode());
        final java.lang.Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "Category(id=" + this.getId() + ", name=" + this.getName() + ", slug=" + this.getSlug() + ", description=" + this.getDescription() + ", active=" + this.isActive() + ")";
    }

    public Category() {
        this.active = Category.$default$active();
    }

    /**
     * Creates a new {@code Category} instance.
     *
     * @param id
     * @param name
     * @param slug Identificador legible usado en las rutas de consulta.
     * @param description
     * @param active
     */

    public Category(final String id, final String name, final String slug, final String description, final boolean active) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.active = active;
    }
}
