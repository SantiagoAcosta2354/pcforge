package com.pcforge.catalogo.domain;

import java.util.List;
import java.util.Map;

/**
 * Atributos tecnicos de un componente. No todos aplican a todos los tipos:
 * una fuente de poder no tiene socket y un procesador no tiene capacidad en vatios.
 * Los campos que no aplican quedan nulos, y el validador de compatibilidad
 * los trata como "dato no disponible" en lugar de asumir un valor.
 */
public class TechnicalSpecs {
    /**
     * Socket del procesador o de la placa base (por ejemplo AM5, LGA1700).
     */
    private String socket;
    /**
     * Tipos de memoria que admite la placa base (por ejemplo DDR4, DDR5).
     */
    private List<String> ramTypeSupported;
    /**
     * Tipo del modulo de memoria (por ejemplo DDR4, DDR5).
     */
    private String ramType;
    /**
     * Consumo declarado en vatios de un componente que consume energia.
     */
    private Integer estimatedPowerW;
    /**
     * Capacidad entregada en vatios, propia de la fuente de poder.
     */
    private Integer capacityW;
    /**
     * Factor de forma cuando aplica (ATX, microATX, ITX).
     */
    private String formFactor;
    /**
     * Atributos adicionales que no participan en las reglas, sin perderlos.
     */
    private Map<String, String> extra;

    public static class TechnicalSpecsBuilder {

        private String socket;

        private List<String> ramTypeSupported;

        private String ramType;

        private Integer estimatedPowerW;

        private Integer capacityW;

        private String formFactor;

        private Map<String, String> extra;

        TechnicalSpecsBuilder() {
        }

        /**
         * Socket del procesador o de la placa base (por ejemplo AM5, LGA1700).
         * @return {@code this}.
         */

        public TechnicalSpecs.TechnicalSpecsBuilder socket(final String socket) {
            this.socket = socket;
            return this;
        }

        /**
         * Tipos de memoria que admite la placa base (por ejemplo DDR4, DDR5).
         * @return {@code this}.
         */

        public TechnicalSpecs.TechnicalSpecsBuilder ramTypeSupported(final List<String> ramTypeSupported) {
            this.ramTypeSupported = ramTypeSupported;
            return this;
        }

        /**
         * Tipo del modulo de memoria (por ejemplo DDR4, DDR5).
         * @return {@code this}.
         */

        public TechnicalSpecs.TechnicalSpecsBuilder ramType(final String ramType) {
            this.ramType = ramType;
            return this;
        }

        /**
         * Consumo declarado en vatios de un componente que consume energia.
         * @return {@code this}.
         */

        public TechnicalSpecs.TechnicalSpecsBuilder estimatedPowerW(final Integer estimatedPowerW) {
            this.estimatedPowerW = estimatedPowerW;
            return this;
        }

        /**
         * Capacidad entregada en vatios, propia de la fuente de poder.
         * @return {@code this}.
         */

        public TechnicalSpecs.TechnicalSpecsBuilder capacityW(final Integer capacityW) {
            this.capacityW = capacityW;
            return this;
        }

        /**
         * Factor de forma cuando aplica (ATX, microATX, ITX).
         * @return {@code this}.
         */

        public TechnicalSpecs.TechnicalSpecsBuilder formFactor(final String formFactor) {
            this.formFactor = formFactor;
            return this;
        }

        /**
         * Atributos adicionales que no participan en las reglas, sin perderlos.
         * @return {@code this}.
         */

        public TechnicalSpecs.TechnicalSpecsBuilder extra(final Map<String, String> extra) {
            this.extra = extra;
            return this;
        }

        public TechnicalSpecs build() {
            return new TechnicalSpecs(this.socket, this.ramTypeSupported, this.ramType, this.estimatedPowerW, this.capacityW, this.formFactor, this.extra);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "TechnicalSpecs.TechnicalSpecsBuilder(socket=" + this.socket + ", ramTypeSupported=" + this.ramTypeSupported + ", ramType=" + this.ramType + ", estimatedPowerW=" + this.estimatedPowerW + ", capacityW=" + this.capacityW + ", formFactor=" + this.formFactor + ", extra=" + this.extra + ")";
        }
    }

    public static TechnicalSpecs.TechnicalSpecsBuilder builder() {
        return new TechnicalSpecs.TechnicalSpecsBuilder();
    }

    /**
     * Socket del procesador o de la placa base (por ejemplo AM5, LGA1700).
     */

    public String getSocket() {
        return this.socket;
    }

    /**
     * Tipos de memoria que admite la placa base (por ejemplo DDR4, DDR5).
     */

    public List<String> getRamTypeSupported() {
        return this.ramTypeSupported;
    }

    /**
     * Tipo del modulo de memoria (por ejemplo DDR4, DDR5).
     */

    public String getRamType() {
        return this.ramType;
    }

    /**
     * Consumo declarado en vatios de un componente que consume energia.
     */

    public Integer getEstimatedPowerW() {
        return this.estimatedPowerW;
    }

    /**
     * Capacidad entregada en vatios, propia de la fuente de poder.
     */

    public Integer getCapacityW() {
        return this.capacityW;
    }

    /**
     * Factor de forma cuando aplica (ATX, microATX, ITX).
     */

    public String getFormFactor() {
        return this.formFactor;
    }

    /**
     * Atributos adicionales que no participan en las reglas, sin perderlos.
     */

    public Map<String, String> getExtra() {
        return this.extra;
    }

    /**
     * Socket del procesador o de la placa base (por ejemplo AM5, LGA1700).
     */

    public void setSocket(final String socket) {
        this.socket = socket;
    }

    /**
     * Tipos de memoria que admite la placa base (por ejemplo DDR4, DDR5).
     */

    public void setRamTypeSupported(final List<String> ramTypeSupported) {
        this.ramTypeSupported = ramTypeSupported;
    }

    /**
     * Tipo del modulo de memoria (por ejemplo DDR4, DDR5).
     */

    public void setRamType(final String ramType) {
        this.ramType = ramType;
    }

    /**
     * Consumo declarado en vatios de un componente que consume energia.
     */

    public void setEstimatedPowerW(final Integer estimatedPowerW) {
        this.estimatedPowerW = estimatedPowerW;
    }

    /**
     * Capacidad entregada en vatios, propia de la fuente de poder.
     */

    public void setCapacityW(final Integer capacityW) {
        this.capacityW = capacityW;
    }

    /**
     * Factor de forma cuando aplica (ATX, microATX, ITX).
     */

    public void setFormFactor(final String formFactor) {
        this.formFactor = formFactor;
    }

    /**
     * Atributos adicionales que no participan en las reglas, sin perderlos.
     */

    public void setExtra(final Map<String, String> extra) {
        this.extra = extra;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof TechnicalSpecs)) return false;
        final TechnicalSpecs other = (TechnicalSpecs) o;
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
        final java.lang.Object this$extra = this.getExtra();
        final java.lang.Object other$extra = other.getExtra();
        if (this$extra == null ? other$extra != null : !this$extra.equals(other$extra)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof TechnicalSpecs;
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
        final java.lang.Object $extra = this.getExtra();
        result = result * PRIME + ($extra == null ? 43 : $extra.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "TechnicalSpecs(socket=" + this.getSocket() + ", ramTypeSupported=" + this.getRamTypeSupported() + ", ramType=" + this.getRamType() + ", estimatedPowerW=" + this.getEstimatedPowerW() + ", capacityW=" + this.getCapacityW() + ", formFactor=" + this.getFormFactor() + ", extra=" + this.getExtra() + ")";
    }

    public TechnicalSpecs() {
    }

    /**
     * Creates a new {@code TechnicalSpecs} instance.
     *
     * @param socket Socket del procesador o de la placa base (por ejemplo AM5, LGA1700).
     * @param ramTypeSupported Tipos de memoria que admite la placa base (por ejemplo DDR4, DDR5).
     * @param ramType Tipo del modulo de memoria (por ejemplo DDR4, DDR5).
     * @param estimatedPowerW Consumo declarado en vatios de un componente que consume energia.
     * @param capacityW Capacidad entregada en vatios, propia de la fuente de poder.
     * @param formFactor Factor de forma cuando aplica (ATX, microATX, ITX).
     * @param extra Atributos adicionales que no participan en las reglas, sin perderlos.
     */

    public TechnicalSpecs(final String socket, final List<String> ramTypeSupported, final String ramType, final Integer estimatedPowerW, final Integer capacityW, final String formFactor, final Map<String, String> extra) {
        this.socket = socket;
        this.ramTypeSupported = ramTypeSupported;
        this.ramType = ramType;
        this.estimatedPowerW = estimatedPowerW;
        this.capacityW = capacityW;
        this.formFactor = formFactor;
        this.extra = extra;
    }
}
