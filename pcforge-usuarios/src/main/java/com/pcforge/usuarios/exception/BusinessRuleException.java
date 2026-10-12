package com.pcforge.usuarios.exception;

/**
 * Regla de negocio incumplida: correo repetido, cuenta inactiva o
 * credenciales incorrectas. Se traduce a HTTP 409 o 401 segun el caso.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
