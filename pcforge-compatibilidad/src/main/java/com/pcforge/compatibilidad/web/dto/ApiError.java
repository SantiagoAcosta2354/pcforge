package com.pcforge.compatibilidad.web.dto;

import java.time.Instant;
import java.util.List;

/** Respuesta de error uniforme para toda la API. */
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
