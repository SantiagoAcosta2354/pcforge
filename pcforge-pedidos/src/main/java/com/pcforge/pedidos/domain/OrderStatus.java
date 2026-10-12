package com.pcforge.pedidos.domain;

/**
 * Estados por los que pasa una solicitud. Los tres primeros cubren la fase
 * comercial (cotizacion) y los siguientes, el servicio de ensamblaje.
 */
public enum OrderStatus {

    /** El cliente registro la solicitud; nadie la ha tomado todavia. */
    RECIBIDO,

    /** El asesor comercial la tomo para revisarla. */
    EN_REVISION,

    /** El asesor respondio con una cotizacion. */
    COTIZADO,

    /** El cliente acepto y el equipo entro a la linea de armado. */
    EN_ENSAMBLAJE,

    /** Se esta verificando el equipo armado. */
    EN_PRUEBAS,

    /** Terminado y disponible para retirar. */
    LISTO_PARA_ENTREGA,

    /** Entregado al cliente. */
    ENTREGADO,

    /** Anulado antes de terminar. */
    CANCELADO;

    /** Los estados en los que el cliente todavia puede anular su solicitud. */
    public boolean isCancellableByCustomer() {
        return this == RECIBIDO || this == EN_REVISION;
    }

    public boolean isClosed() {
        return this == ENTREGADO || this == CANCELADO;
    }
}
