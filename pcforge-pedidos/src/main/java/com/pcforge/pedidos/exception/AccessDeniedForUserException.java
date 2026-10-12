package com.pcforge.pedidos.exception;

/** El usuario autenticado no puede acceder a este pedido. Se traduce a HTTP 403. */
public class AccessDeniedForUserException extends RuntimeException {

    public AccessDeniedForUserException(String message) {
        super(message);
    }
}
