package com.pcforge.pedidos.exception;

/**
 * Un microservicio del que depende esta operacion no respondio. Se traduce a
 * HTTP 503 y, en el caso del descuento de inventario, impide el cambio de
 * estado: es preferible rechazar la operacion que dejar el inventario
 * desincronizado sin saberlo.
 */
public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String message) {
        super(message);
    }
}
