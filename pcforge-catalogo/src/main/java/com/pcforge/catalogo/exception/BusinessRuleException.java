package com.pcforge.catalogo.exception;

/**
 * Se lanza cuando la operacion es valida en forma pero rompe una regla de
 * negocio (SKU repetido, existencia negativa, producto inactivo). HTTP 422
 * o 409 segun el caso.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
