package com.pcforge.web.exception;

/** El recurso consultado no existe. Se traduce a la pagina 404. */
public class NotFoundInViewException extends RuntimeException {

    public NotFoundInViewException(String message) {
        super(message);
    }
}
