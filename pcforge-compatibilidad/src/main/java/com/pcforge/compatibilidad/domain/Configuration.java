package com.pcforge.compatibilidad.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Configuracion de equipos armada por un usuario. Pertenece a la persona que
 * la crea: un cliente solo puede ver y modificar las suyas.
 */
@Document(collection = "configurations")
public class Configuration {
    @Id
    private String id;
    private String ownerId;
    private String ownerEmail;
    private String name;
    private List<ConfigurationItem> items;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    /**
     * Ultimo resultado global de la validacion, si ya se ejecuto alguna.
     */
    private String lastResult;

    private static List<ConfigurationItem> $default$items() {
        return new ArrayList<>();
    }

    public static class ConfigurationBuilder {

        private String id;

        private String ownerId;

        private String ownerEmail;

        private String name;

        private boolean items$set;

        private List<ConfigurationItem> items$value;

        private Instant createdAt;

        private Instant updatedAt;

        private String lastResult;

        ConfigurationBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder ownerId(final String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder ownerEmail(final String ownerEmail) {
            this.ownerEmail = ownerEmail;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder items(final List<ConfigurationItem> items) {
            this.items$value = items;
            items$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * Ultimo resultado global de la validacion, si ya se ejecuto alguna.
         * @return {@code this}.
         */

        public Configuration.ConfigurationBuilder lastResult(final String lastResult) {
            this.lastResult = lastResult;
            return this;
        }

        public Configuration build() {
            List<ConfigurationItem> items$value = this.items$value;
            if (!this.items$set) items$value = Configuration.$default$items();
            return new Configuration(this.id, this.ownerId, this.ownerEmail, this.name, items$value, this.createdAt, this.updatedAt, this.lastResult);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "Configuration.ConfigurationBuilder(id=" + this.id + ", ownerId=" + this.ownerId + ", ownerEmail=" + this.ownerEmail + ", name=" + this.name + ", items$value=" + this.items$value + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", lastResult=" + this.lastResult + ")";
        }
    }

    public static Configuration.ConfigurationBuilder builder() {
        return new Configuration.ConfigurationBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public String getOwnerEmail() {
        return this.ownerEmail;
    }

    public String getName() {
        return this.name;
    }

    public List<ConfigurationItem> getItems() {
        return this.items;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    /**
     * Ultimo resultado global de la validacion, si ya se ejecuto alguna.
     */

    public String getLastResult() {
        return this.lastResult;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setOwnerId(final String ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerEmail(final String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setItems(final List<ConfigurationItem> items) {
        this.items = items;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Ultimo resultado global de la validacion, si ya se ejecuto alguna.
     */

    public void setLastResult(final String lastResult) {
        this.lastResult = lastResult;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Configuration)) return false;
        final Configuration other = (Configuration) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$ownerId = this.getOwnerId();
        final java.lang.Object other$ownerId = other.getOwnerId();
        if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
        final java.lang.Object this$ownerEmail = this.getOwnerEmail();
        final java.lang.Object other$ownerEmail = other.getOwnerEmail();
        if (this$ownerEmail == null ? other$ownerEmail != null : !this$ownerEmail.equals(other$ownerEmail)) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$items = this.getItems();
        final java.lang.Object other$items = other.getItems();
        if (this$items == null ? other$items != null : !this$items.equals(other$items)) return false;
        final java.lang.Object this$createdAt = this.getCreatedAt();
        final java.lang.Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        final java.lang.Object this$updatedAt = this.getUpdatedAt();
        final java.lang.Object other$updatedAt = other.getUpdatedAt();
        if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
        final java.lang.Object this$lastResult = this.getLastResult();
        final java.lang.Object other$lastResult = other.getLastResult();
        if (this$lastResult == null ? other$lastResult != null : !this$lastResult.equals(other$lastResult)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Configuration;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $ownerId = this.getOwnerId();
        result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
        final java.lang.Object $ownerEmail = this.getOwnerEmail();
        result = result * PRIME + ($ownerEmail == null ? 43 : $ownerEmail.hashCode());
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $items = this.getItems();
        result = result * PRIME + ($items == null ? 43 : $items.hashCode());
        final java.lang.Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        final java.lang.Object $updatedAt = this.getUpdatedAt();
        result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
        final java.lang.Object $lastResult = this.getLastResult();
        result = result * PRIME + ($lastResult == null ? 43 : $lastResult.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "Configuration(id=" + this.getId() + ", ownerId=" + this.getOwnerId() + ", ownerEmail=" + this.getOwnerEmail() + ", name=" + this.getName() + ", items=" + this.getItems() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ", lastResult=" + this.getLastResult() + ")";
    }

    public Configuration() {
        this.items = Configuration.$default$items();
    }

    /**
     * Creates a new {@code Configuration} instance.
     *
     * @param id
     * @param ownerId
     * @param ownerEmail
     * @param name
     * @param items
     * @param createdAt
     * @param updatedAt
     * @param lastResult Ultimo resultado global de la validacion, si ya se ejecuto alguna.
     */

    public Configuration(final String id, final String ownerId, final String ownerEmail, final String name, final List<ConfigurationItem> items, final Instant createdAt, final Instant updatedAt, final String lastResult) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.name = name;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastResult = lastResult;
    }
}
