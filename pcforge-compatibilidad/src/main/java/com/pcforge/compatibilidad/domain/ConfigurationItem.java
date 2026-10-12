package com.pcforge.compatibilidad.domain;

/**
 * Componente dentro de una configuracion. Guarda una copia de los atributos
 * tecnicos y del precio vigentes al momento de agregarlo, de modo que el
 * resultado de una validacion anterior pueda explicarse despues aunque el
 * catalogo haya cambiado.
 */
public class ConfigurationItem {
    private String itemId;
    private String productId;
    private String sku;
    private String name;
    private String componentType;
    private java.math.BigDecimal price;
    private int quantity;
    /**
     * Atributos tecnicos copiados del catalogo.
     */
    private ItemSpecs specs;

    public static class ItemSpecs {
        private String socket;
        private java.util.List<String> ramTypeSupported;
        private String ramType;
        private Integer estimatedPowerW;
        private Integer capacityW;
        private String formFactor;

        public static class ItemSpecsBuilder {

            private String socket;

            private java.util.List<String> ramTypeSupported;

            private String ramType;

            private Integer estimatedPowerW;

            private Integer capacityW;

            private String formFactor;

            ItemSpecsBuilder() {
            }

            /**
             * @return {@code this}.
             */

            public ConfigurationItem.ItemSpecs.ItemSpecsBuilder socket(final String socket) {
                this.socket = socket;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public ConfigurationItem.ItemSpecs.ItemSpecsBuilder ramTypeSupported(final java.util.List<String> ramTypeSupported) {
                this.ramTypeSupported = ramTypeSupported;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public ConfigurationItem.ItemSpecs.ItemSpecsBuilder ramType(final String ramType) {
                this.ramType = ramType;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public ConfigurationItem.ItemSpecs.ItemSpecsBuilder estimatedPowerW(final Integer estimatedPowerW) {
                this.estimatedPowerW = estimatedPowerW;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public ConfigurationItem.ItemSpecs.ItemSpecsBuilder capacityW(final Integer capacityW) {
                this.capacityW = capacityW;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public ConfigurationItem.ItemSpecs.ItemSpecsBuilder formFactor(final String formFactor) {
                this.formFactor = formFactor;
                return this;
            }

            public ConfigurationItem.ItemSpecs build() {
                return new ConfigurationItem.ItemSpecs(this.socket, this.ramTypeSupported, this.ramType, this.estimatedPowerW, this.capacityW, this.formFactor);
            }

            @java.lang.Override

            public java.lang.String toString() {
                return "ConfigurationItem.ItemSpecs.ItemSpecsBuilder(socket=" + this.socket + ", ramTypeSupported=" + this.ramTypeSupported + ", ramType=" + this.ramType + ", estimatedPowerW=" + this.estimatedPowerW + ", capacityW=" + this.capacityW + ", formFactor=" + this.formFactor + ")";
            }
        }

        public static ConfigurationItem.ItemSpecs.ItemSpecsBuilder builder() {
            return new ConfigurationItem.ItemSpecs.ItemSpecsBuilder();
        }

        public String getSocket() {
            return this.socket;
        }

        public java.util.List<String> getRamTypeSupported() {
            return this.ramTypeSupported;
        }

        public String getRamType() {
            return this.ramType;
        }

        public Integer getEstimatedPowerW() {
            return this.estimatedPowerW;
        }

        public Integer getCapacityW() {
            return this.capacityW;
        }

        public String getFormFactor() {
            return this.formFactor;
        }

        public void setSocket(final String socket) {
            this.socket = socket;
        }

        public void setRamTypeSupported(final java.util.List<String> ramTypeSupported) {
            this.ramTypeSupported = ramTypeSupported;
        }

        public void setRamType(final String ramType) {
            this.ramType = ramType;
        }

        public void setEstimatedPowerW(final Integer estimatedPowerW) {
            this.estimatedPowerW = estimatedPowerW;
        }

        public void setCapacityW(final Integer capacityW) {
            this.capacityW = capacityW;
        }

        public void setFormFactor(final String formFactor) {
            this.formFactor = formFactor;
        }

        @java.lang.Override

        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof ConfigurationItem.ItemSpecs)) return false;
            final ConfigurationItem.ItemSpecs other = (ConfigurationItem.ItemSpecs) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$estimatedPowerW = this.getEstimatedPowerW();
            final java.lang.Object other$estimatedPowerW = other.getEstimatedPowerW();
            if (this$estimatedPowerW == null ? other$estimatedPowerW != null : !this$estimatedPowerW.equals(other$estimatedPowerW)) return false;
            final java.lang.Object this$capacityW = this.getCapacityW();
            final java.lang.Object other$capacityW = other.getCapacityW();
            if (this$capacityW == null ? other$capacityW != null : !this$capacityW.equals(other$capacityW)) return false;
            final java.lang.Object this$socket = this.getSocket();
            final java.lang.Object other$socket = other.getSocket();
            if (this$socket == null ? other$socket != null : !this$socket.equals(other$socket)) return false;
            final java.lang.Object this$ramTypeSupported = this.getRamTypeSupported();
            final java.lang.Object other$ramTypeSupported = other.getRamTypeSupported();
            if (this$ramTypeSupported == null ? other$ramTypeSupported != null : !this$ramTypeSupported.equals(other$ramTypeSupported)) return false;
            final java.lang.Object this$ramType = this.getRamType();
            final java.lang.Object other$ramType = other.getRamType();
            if (this$ramType == null ? other$ramType != null : !this$ramType.equals(other$ramType)) return false;
            final java.lang.Object this$formFactor = this.getFormFactor();
            final java.lang.Object other$formFactor = other.getFormFactor();
            if (this$formFactor == null ? other$formFactor != null : !this$formFactor.equals(other$formFactor)) return false;
            return true;
        }

        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof ConfigurationItem.ItemSpecs;
        }

        @java.lang.Override

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $estimatedPowerW = this.getEstimatedPowerW();
            result = result * PRIME + ($estimatedPowerW == null ? 43 : $estimatedPowerW.hashCode());
            final java.lang.Object $capacityW = this.getCapacityW();
            result = result * PRIME + ($capacityW == null ? 43 : $capacityW.hashCode());
            final java.lang.Object $socket = this.getSocket();
            result = result * PRIME + ($socket == null ? 43 : $socket.hashCode());
            final java.lang.Object $ramTypeSupported = this.getRamTypeSupported();
            result = result * PRIME + ($ramTypeSupported == null ? 43 : $ramTypeSupported.hashCode());
            final java.lang.Object $ramType = this.getRamType();
            result = result * PRIME + ($ramType == null ? 43 : $ramType.hashCode());
            final java.lang.Object $formFactor = this.getFormFactor();
            result = result * PRIME + ($formFactor == null ? 43 : $formFactor.hashCode());
            return result;
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "ConfigurationItem.ItemSpecs(socket=" + this.getSocket() + ", ramTypeSupported=" + this.getRamTypeSupported() + ", ramType=" + this.getRamType() + ", estimatedPowerW=" + this.getEstimatedPowerW() + ", capacityW=" + this.getCapacityW() + ", formFactor=" + this.getFormFactor() + ")";
        }

        public ItemSpecs() {
        }

        public ItemSpecs(final String socket, final java.util.List<String> ramTypeSupported, final String ramType, final Integer estimatedPowerW, final Integer capacityW, final String formFactor) {
            this.socket = socket;
            this.ramTypeSupported = ramTypeSupported;
            this.ramType = ramType;
            this.estimatedPowerW = estimatedPowerW;
            this.capacityW = capacityW;
            this.formFactor = formFactor;
        }
    }

    private static int $default$quantity() {
        return 1;
    }

    public static class ConfigurationItemBuilder {

        private String itemId;

        private String productId;

        private String sku;

        private String name;

        private String componentType;

        private java.math.BigDecimal price;

        private boolean quantity$set;

        private int quantity$value;

        private ItemSpecs specs;

        ConfigurationItemBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder itemId(final String itemId) {
            this.itemId = itemId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder productId(final String productId) {
            this.productId = productId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder sku(final String sku) {
            this.sku = sku;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder componentType(final String componentType) {
            this.componentType = componentType;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder price(final java.math.BigDecimal price) {
            this.price = price;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder quantity(final int quantity) {
            this.quantity$value = quantity;
            quantity$set = true;
            return this;
        }

        /**
         * Atributos tecnicos copiados del catalogo.
         * @return {@code this}.
         */

        public ConfigurationItem.ConfigurationItemBuilder specs(final ItemSpecs specs) {
            this.specs = specs;
            return this;
        }

        public ConfigurationItem build() {
            int quantity$value = this.quantity$value;
            if (!this.quantity$set) quantity$value = ConfigurationItem.$default$quantity();
            return new ConfigurationItem(this.itemId, this.productId, this.sku, this.name, this.componentType, this.price, quantity$value, this.specs);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "ConfigurationItem.ConfigurationItemBuilder(itemId=" + this.itemId + ", productId=" + this.productId + ", sku=" + this.sku + ", name=" + this.name + ", componentType=" + this.componentType + ", price=" + this.price + ", quantity$value=" + this.quantity$value + ", specs=" + this.specs + ")";
        }
    }

    public static ConfigurationItem.ConfigurationItemBuilder builder() {
        return new ConfigurationItem.ConfigurationItemBuilder();
    }

    public String getItemId() {
        return this.itemId;
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

    public java.math.BigDecimal getPrice() {
        return this.price;
    }

    public int getQuantity() {
        return this.quantity;
    }

    /**
     * Atributos tecnicos copiados del catalogo.
     */

    public ItemSpecs getSpecs() {
        return this.specs;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
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

    public void setPrice(final java.math.BigDecimal price) {
        this.price = price;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    /**
     * Atributos tecnicos copiados del catalogo.
     */

    public void setSpecs(final ItemSpecs specs) {
        this.specs = specs;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ConfigurationItem)) return false;
        final ConfigurationItem other = (ConfigurationItem) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.getQuantity() != other.getQuantity()) return false;
        final java.lang.Object this$itemId = this.getItemId();
        final java.lang.Object other$itemId = other.getItemId();
        if (this$itemId == null ? other$itemId != null : !this$itemId.equals(other$itemId)) return false;
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
        final java.lang.Object this$price = this.getPrice();
        final java.lang.Object other$price = other.getPrice();
        if (this$price == null ? other$price != null : !this$price.equals(other$price)) return false;
        final java.lang.Object this$specs = this.getSpecs();
        final java.lang.Object other$specs = other.getSpecs();
        if (this$specs == null ? other$specs != null : !this$specs.equals(other$specs)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ConfigurationItem;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getQuantity();
        final java.lang.Object $itemId = this.getItemId();
        result = result * PRIME + ($itemId == null ? 43 : $itemId.hashCode());
        final java.lang.Object $productId = this.getProductId();
        result = result * PRIME + ($productId == null ? 43 : $productId.hashCode());
        final java.lang.Object $sku = this.getSku();
        result = result * PRIME + ($sku == null ? 43 : $sku.hashCode());
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $componentType = this.getComponentType();
        result = result * PRIME + ($componentType == null ? 43 : $componentType.hashCode());
        final java.lang.Object $price = this.getPrice();
        result = result * PRIME + ($price == null ? 43 : $price.hashCode());
        final java.lang.Object $specs = this.getSpecs();
        result = result * PRIME + ($specs == null ? 43 : $specs.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "ConfigurationItem(itemId=" + this.getItemId() + ", productId=" + this.getProductId() + ", sku=" + this.getSku() + ", name=" + this.getName() + ", componentType=" + this.getComponentType() + ", price=" + this.getPrice() + ", quantity=" + this.getQuantity() + ", specs=" + this.getSpecs() + ")";
    }

    public ConfigurationItem() {
        this.quantity = ConfigurationItem.$default$quantity();
    }

    /**
     * Creates a new {@code ConfigurationItem} instance.
     *
     * @param itemId
     * @param productId
     * @param sku
     * @param name
     * @param componentType
     * @param price
     * @param quantity
     * @param specs Atributos tecnicos copiados del catalogo.
     */

    public ConfigurationItem(final String itemId, final String productId, final String sku, final String name, final String componentType, final java.math.BigDecimal price, final int quantity, final ItemSpecs specs) {
        this.itemId = itemId;
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.componentType = componentType;
        this.price = price;
        this.quantity = quantity;
        this.specs = specs;
    }
}
