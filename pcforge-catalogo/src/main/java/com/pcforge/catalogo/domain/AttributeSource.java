package com.pcforge.catalogo.domain;

import java.time.Instant;

/**
 * Evidencia de un atributo tecnico: de donde salio el dato y cuando se
 * verifico. Sin esta evidencia un atributo no puede sostener una validacion
 * de compatibilidad, porque no hay forma de explicar de donde proviene.
 */
public class AttributeSource {

    /** Referencia de la fuente: ficha del fabricante, URL o documento interno. */
    private String source;

    /** Momento en que el curador verifico el dato. */
    private Instant verifiedAt;

    /** Identificador del usuario que registro la verificacion. */
    private String verifiedBy;

    public AttributeSource() {
    }

    public AttributeSource(final String source, final Instant verifiedAt, final String verifiedBy) {
        this.source = source;
        this.verifiedAt = verifiedAt;
        this.verifiedBy = verifiedBy;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public Instant getVerifiedAt() {
        return this.verifiedAt;
    }

    public void setVerifiedAt(final Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedBy() {
        return this.verifiedBy;
    }

    public void setVerifiedBy(final String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    @Override
    public String toString() {
        return "AttributeSource(source=" + this.getSource() + ", verifiedAt=" + this.getVerifiedAt()
                + ", verifiedBy=" + this.getVerifiedBy() + ")";
    }
}
