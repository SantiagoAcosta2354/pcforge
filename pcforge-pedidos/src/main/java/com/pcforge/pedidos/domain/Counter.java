package com.pcforge.pedidos.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Contador para generar numeros de pedido consecutivos. Se incrementa de forma
 * atomica en MongoDB, de modo que dos solicitudes simultaneas no reciban el
 * mismo numero aunque las atienda el mismo proceso.
 */
@Document(collection = "counters")
public class Counter {
    @Id
    private String id;
    private long seq;

    public String getId() {
        return this.id;
    }

    public long getSeq() {
        return this.seq;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setSeq(final long seq) {
        this.seq = seq;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Counter)) return false;
        final Counter other = (Counter) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.getSeq() != other.getSeq()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Counter;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $seq = this.getSeq();
        result = result * PRIME + (int) ($seq >>> 32 ^ $seq);
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "Counter(id=" + this.getId() + ", seq=" + this.getSeq() + ")";
    }

    public Counter() {
    }

    public Counter(final String id, final long seq) {
        this.id = id;
        this.seq = seq;
    }
}
