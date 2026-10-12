package com.pcforge.compatibilidad.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Evidencia de una validacion. Se conserva para poder explicar despues por que
 * el sistema respondio lo que respondio.
 */
@Document(collection = "validationResults")
public class ValidationResult {
    @Id
    private String id;
    private String configurationId;
    private String requestedBy;
    /**
     * Resultado global: OK, INCOMPATIBLE o NO_CONCLUYENTE.
     */
    private CheckStatus result;
    private List<RuleCheck> checks;
    /**
     * Atributos tecnicos que faltaron y que impidieron concluir.
     */
    private List<String> missingData;
    private Instant createdAt;

    public static class RuleCheck {
        private RuleCode rule;
        private CheckStatus status;
        private String message;

        public static class RuleCheckBuilder {

            private RuleCode rule;

            private CheckStatus status;

            private String message;

            RuleCheckBuilder() {
            }

            /**
             * @return {@code this}.
             */

            public ValidationResult.RuleCheck.RuleCheckBuilder rule(final RuleCode rule) {
                this.rule = rule;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public ValidationResult.RuleCheck.RuleCheckBuilder status(final CheckStatus status) {
                this.status = status;
                return this;
            }

            /**
             * @return {@code this}.
             */

            public ValidationResult.RuleCheck.RuleCheckBuilder message(final String message) {
                this.message = message;
                return this;
            }

            public ValidationResult.RuleCheck build() {
                return new ValidationResult.RuleCheck(this.rule, this.status, this.message);
            }

            @java.lang.Override

            public java.lang.String toString() {
                return "ValidationResult.RuleCheck.RuleCheckBuilder(rule=" + this.rule + ", status=" + this.status + ", message=" + this.message + ")";
            }
        }

        public static ValidationResult.RuleCheck.RuleCheckBuilder builder() {
            return new ValidationResult.RuleCheck.RuleCheckBuilder();
        }

        public RuleCode getRule() {
            return this.rule;
        }

        public CheckStatus getStatus() {
            return this.status;
        }

        public String getMessage() {
            return this.message;
        }

        public void setRule(final RuleCode rule) {
            this.rule = rule;
        }

        public void setStatus(final CheckStatus status) {
            this.status = status;
        }

        public void setMessage(final String message) {
            this.message = message;
        }

        @java.lang.Override

        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof ValidationResult.RuleCheck)) return false;
            final ValidationResult.RuleCheck other = (ValidationResult.RuleCheck) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$rule = this.getRule();
            final java.lang.Object other$rule = other.getRule();
            if (this$rule == null ? other$rule != null : !this$rule.equals(other$rule)) return false;
            final java.lang.Object this$status = this.getStatus();
            final java.lang.Object other$status = other.getStatus();
            if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
            final java.lang.Object this$message = this.getMessage();
            final java.lang.Object other$message = other.getMessage();
            if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
            return true;
        }

        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof ValidationResult.RuleCheck;
        }

        @java.lang.Override

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $rule = this.getRule();
            result = result * PRIME + ($rule == null ? 43 : $rule.hashCode());
            final java.lang.Object $status = this.getStatus();
            result = result * PRIME + ($status == null ? 43 : $status.hashCode());
            final java.lang.Object $message = this.getMessage();
            result = result * PRIME + ($message == null ? 43 : $message.hashCode());
            return result;
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "ValidationResult.RuleCheck(rule=" + this.getRule() + ", status=" + this.getStatus() + ", message=" + this.getMessage() + ")";
        }

        public RuleCheck() {
        }

        public RuleCheck(final RuleCode rule, final CheckStatus status, final String message) {
            this.rule = rule;
            this.status = status;
            this.message = message;
        }
    }

    private static List<RuleCheck> $default$checks() {
        return new ArrayList<>();
    }

    private static List<String> $default$missingData() {
        return new ArrayList<>();
    }

    public static class ValidationResultBuilder {

        private String id;

        private String configurationId;

        private String requestedBy;

        private CheckStatus result;

        private boolean checks$set;

        private List<RuleCheck> checks$value;

        private boolean missingData$set;

        private List<String> missingData$value;

        private Instant createdAt;

        ValidationResultBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public ValidationResult.ValidationResultBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ValidationResult.ValidationResultBuilder configurationId(final String configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ValidationResult.ValidationResultBuilder requestedBy(final String requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }

        /**
         * Resultado global: OK, INCOMPATIBLE o NO_CONCLUYENTE.
         * @return {@code this}.
         */

        public ValidationResult.ValidationResultBuilder result(final CheckStatus result) {
            this.result = result;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ValidationResult.ValidationResultBuilder checks(final List<RuleCheck> checks) {
            this.checks$value = checks;
            checks$set = true;
            return this;
        }

        /**
         * Atributos tecnicos que faltaron y que impidieron concluir.
         * @return {@code this}.
         */

        public ValidationResult.ValidationResultBuilder missingData(final List<String> missingData) {
            this.missingData$value = missingData;
            missingData$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public ValidationResult.ValidationResultBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ValidationResult build() {
            List<RuleCheck> checks$value = this.checks$value;
            if (!this.checks$set) checks$value = ValidationResult.$default$checks();
            List<String> missingData$value = this.missingData$value;
            if (!this.missingData$set) missingData$value = ValidationResult.$default$missingData();
            return new ValidationResult(this.id, this.configurationId, this.requestedBy, this.result, checks$value, missingData$value, this.createdAt);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "ValidationResult.ValidationResultBuilder(id=" + this.id + ", configurationId=" + this.configurationId + ", requestedBy=" + this.requestedBy + ", result=" + this.result + ", checks$value=" + this.checks$value + ", missingData$value=" + this.missingData$value + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static ValidationResult.ValidationResultBuilder builder() {
        return new ValidationResult.ValidationResultBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getConfigurationId() {
        return this.configurationId;
    }

    public String getRequestedBy() {
        return this.requestedBy;
    }

    /**
     * Resultado global: OK, INCOMPATIBLE o NO_CONCLUYENTE.
     */

    public CheckStatus getResult() {
        return this.result;
    }

    public List<RuleCheck> getChecks() {
        return this.checks;
    }

    /**
     * Atributos tecnicos que faltaron y que impidieron concluir.
     */

    public List<String> getMissingData() {
        return this.missingData;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setConfigurationId(final String configurationId) {
        this.configurationId = configurationId;
    }

    public void setRequestedBy(final String requestedBy) {
        this.requestedBy = requestedBy;
    }

    /**
     * Resultado global: OK, INCOMPATIBLE o NO_CONCLUYENTE.
     */

    public void setResult(final CheckStatus result) {
        this.result = result;
    }

    public void setChecks(final List<RuleCheck> checks) {
        this.checks = checks;
    }

    /**
     * Atributos tecnicos que faltaron y que impidieron concluir.
     */

    public void setMissingData(final List<String> missingData) {
        this.missingData = missingData;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ValidationResult)) return false;
        final ValidationResult other = (ValidationResult) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$configurationId = this.getConfigurationId();
        final java.lang.Object other$configurationId = other.getConfigurationId();
        if (this$configurationId == null ? other$configurationId != null : !this$configurationId.equals(other$configurationId)) return false;
        final java.lang.Object this$requestedBy = this.getRequestedBy();
        final java.lang.Object other$requestedBy = other.getRequestedBy();
        if (this$requestedBy == null ? other$requestedBy != null : !this$requestedBy.equals(other$requestedBy)) return false;
        final java.lang.Object this$result = this.getResult();
        final java.lang.Object other$result = other.getResult();
        if (this$result == null ? other$result != null : !this$result.equals(other$result)) return false;
        final java.lang.Object this$checks = this.getChecks();
        final java.lang.Object other$checks = other.getChecks();
        if (this$checks == null ? other$checks != null : !this$checks.equals(other$checks)) return false;
        final java.lang.Object this$missingData = this.getMissingData();
        final java.lang.Object other$missingData = other.getMissingData();
        if (this$missingData == null ? other$missingData != null : !this$missingData.equals(other$missingData)) return false;
        final java.lang.Object this$createdAt = this.getCreatedAt();
        final java.lang.Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ValidationResult;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $configurationId = this.getConfigurationId();
        result = result * PRIME + ($configurationId == null ? 43 : $configurationId.hashCode());
        final java.lang.Object $requestedBy = this.getRequestedBy();
        result = result * PRIME + ($requestedBy == null ? 43 : $requestedBy.hashCode());
        final java.lang.Object $result = this.getResult();
        result = result * PRIME + ($result == null ? 43 : $result.hashCode());
        final java.lang.Object $checks = this.getChecks();
        result = result * PRIME + ($checks == null ? 43 : $checks.hashCode());
        final java.lang.Object $missingData = this.getMissingData();
        result = result * PRIME + ($missingData == null ? 43 : $missingData.hashCode());
        final java.lang.Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "ValidationResult(id=" + this.getId() + ", configurationId=" + this.getConfigurationId() + ", requestedBy=" + this.getRequestedBy() + ", result=" + this.getResult() + ", checks=" + this.getChecks() + ", missingData=" + this.getMissingData() + ", createdAt=" + this.getCreatedAt() + ")";
    }

    public ValidationResult() {
        this.checks = ValidationResult.$default$checks();
        this.missingData = ValidationResult.$default$missingData();
    }

    /**
     * Creates a new {@code ValidationResult} instance.
     *
     * @param id
     * @param configurationId
     * @param requestedBy
     * @param result Resultado global: OK, INCOMPATIBLE o NO_CONCLUYENTE.
     * @param checks
     * @param missingData Atributos tecnicos que faltaron y que impidieron concluir.
     * @param createdAt
     */

    public ValidationResult(final String id, final String configurationId, final String requestedBy, final CheckStatus result, final List<RuleCheck> checks, final List<String> missingData, final Instant createdAt) {
        this.id = id;
        this.configurationId = configurationId;
        this.requestedBy = requestedBy;
        this.result = result;
        this.checks = checks;
        this.missingData = missingData;
        this.createdAt = createdAt;
    }
}
