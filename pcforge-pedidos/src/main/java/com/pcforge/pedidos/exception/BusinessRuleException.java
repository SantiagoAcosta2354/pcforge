package com.pcforge.pedidos.exception;

/** Regla de negocio incumplida. Se traduce a HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
