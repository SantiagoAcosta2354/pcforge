package com.pcforge.web.exception;

/**
 * Regla de negocio incumplida en alguno de los servicios. El mensaje se
 * muestra tal cual al usuario porque lo redacto el servicio que es dueño de
 * la regla y ya viene en lenguaje claro.
 */
public class BusinessMessageException extends RuntimeException {

    public BusinessMessageException(String message) {
        super(message);
    }
}
