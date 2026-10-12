package com.pcforge.compatibilidad.exception;

/**
 * El catalogo no respondio. Se traduce a HTTP 503.
 * Es importante que este caso nunca se confunda con un resultado de
 * compatibilidad: si no se pudo consultar, no se pudo validar.
 */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message) {
        super(message);
    }
}
