package com.pcforge.catalogo.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * Respuesta de error uniforme para toda la API. Evita exponer trazas internas
 * y entrega al consumidor el campo o la regla que produjo el problema.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {

    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, message, path, details);
    }
}
