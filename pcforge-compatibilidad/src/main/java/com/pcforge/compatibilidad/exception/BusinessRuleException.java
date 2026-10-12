package com.pcforge.compatibilidad.exception;

/** Regla de negocio incumplida. Se traduce a HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
