package com.pcforge.catalogo.exception;

/** Se lanza cuando se busca un recurso que no existe. Se traduce a HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
