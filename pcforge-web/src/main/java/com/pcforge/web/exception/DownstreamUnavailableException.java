package com.pcforge.web.exception;

/**
 * Un microservicio no respondio. Se muestra una pantalla amable en lugar de un
 * error tecnico, porque en el plan gratuito de la nube los servicios se
 * suspenden cuando llevan un rato sin uso y tardan en despertar.
 */
public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String message) {
        super(message);
    }

    public DownstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
