package com.pcforge.compatibilidad.exception;

/**
 * El usuario autenticado no es el dueño de la configuracion y no tiene un rol
 * de personal. Se traduce a HTTP 403, no a 404, porque el recurso existe.
 */
public class AccessDeniedForUserException extends RuntimeException {

    public AccessDeniedForUserException(String message) {
        super(message);
    }
}
