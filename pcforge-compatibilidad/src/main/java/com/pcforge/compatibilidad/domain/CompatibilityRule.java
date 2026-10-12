package com.pcforge.compatibilidad.domain;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Parametros de una regla. Se guardan en la base y no en el codigo para poder
 * ajustarlos (por ejemplo el margen de potencia) sin recompilar.
 */
@Document(collection = "compatibilityRules")
public class CompatibilityRule {
    @Id
    private String id;
    @Indexed(unique = true)
    private RuleCode code;
    private String name;
    private String description;
    private boolean active;
    /**
     * Parametros de la regla, por ejemplo {"marginPercent": "20"}.
     */
    private Map<String, String> parameters;
    @LastModifiedDate
    private Instant updatedAt;

    private static boolean $default$active() {
        return true;
    }

    public static class CompatibilityRuleBuilder {

        private String id;

        private RuleCode code;

        private String name;

        private String description;

        private boolean active$set;

        private boolean active$value;

        private Map<String, String> parameters;

        private Instant updatedAt;

        CompatibilityRuleBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public CompatibilityRule.CompatibilityRuleBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public CompatibilityRule.CompatibilityRuleBuilder code(final RuleCode code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public CompatibilityRule.CompatibilityRuleBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public CompatibilityRule.CompatibilityRuleBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public CompatibilityRule.CompatibilityRuleBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * Parametros de la regla, por ejemplo {"marginPercent": "20"}.
         * @return {@code this}.
         */

        public CompatibilityRule.CompatibilityRuleBuilder parameters(final Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public CompatibilityRule.CompatibilityRuleBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CompatibilityRule build() {
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = CompatibilityRule.$default$active();
            return new CompatibilityRule(this.id, this.code, this.name, this.description, active$value, this.parameters, this.updatedAt);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "CompatibilityRule.CompatibilityRuleBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", active$value=" + this.active$value + ", parameters=" + this.parameters + ", updatedAt=" + this.updatedAt + ")";
        }
    }

    public static CompatibilityRule.CompatibilityRuleBuilder builder() {
        return new CompatibilityRule.CompatibilityRuleBuilder();
    }

    public String getId() {
        return this.id;
    }

    public RuleCode getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isActive() {
        return this.active;
    }

    /**
     * Parametros de la regla, por ejemplo {"marginPercent": "20"}.
     */

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setCode(final RuleCode code) {
        this.code = code;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Parametros de la regla, por ejemplo {"marginPercent": "20"}.
     */

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof CompatibilityRule)) return false;
        final CompatibilityRule other = (CompatibilityRule) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.isActive() != other.isActive()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$code = this.getCode();
        final java.lang.Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$description = this.getDescription();
        final java.lang.Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
        final java.lang.Object this$parameters = this.getParameters();
        final java.lang.Object other$parameters = other.getParameters();
        if (this$parameters == null ? other$parameters != null : !this$parameters.equals(other$parameters)) return false;
        final java.lang.Object this$updatedAt = this.getUpdatedAt();
        final java.lang.Object other$updatedAt = other.getUpdatedAt();
        if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof CompatibilityRule;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isActive() ? 79 : 97);
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final java.lang.Object $parameters = this.getParameters();
        result = result * PRIME + ($parameters == null ? 43 : $parameters.hashCode());
        final java.lang.Object $updatedAt = this.getUpdatedAt();
        result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "CompatibilityRule(id=" + this.getId() + ", code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", active=" + this.isActive() + ", parameters=" + this.getParameters() + ", updatedAt=" + this.getUpdatedAt() + ")";
    }

    public CompatibilityRule() {
        this.active = CompatibilityRule.$default$active();
    }

    /**
     * Creates a new {@code CompatibilityRule} instance.
     *
     * @param id
     * @param code
     * @param name
     * @param description
     * @param active
     * @param parameters Parametros de la regla, por ejemplo {"marginPercent": "20"}.
     * @param updatedAt
     */

    public CompatibilityRule(final String id, final RuleCode code, final String name, final String description, final boolean active, final Map<String, String> parameters, final Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.parameters = parameters;
        this.updatedAt = updatedAt;
    }
}
