package com.pcforge.compatibilidad.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.pcforge.compatibilidad.exception.AccessDeniedForUserException;
import com.pcforge.compatibilidad.exception.BusinessRuleException;
import com.pcforge.compatibilidad.exception.CatalogUnavailableException;
import com.pcforge.compatibilidad.exception.ResourceNotFoundException;
import com.pcforge.compatibilidad.web.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedForUserException.class)
    public ResponseEntity<ApiError> handleForbidden(AccessDeniedForUserException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Regla de negocio incumplida", ex.getMessage(), request);
    }

    /**
     * El catalogo no respondio. Se responde 503 y nunca un resultado de
     * compatibilidad: si no se pudo consultar, no se pudo validar.
     */
    @ExceptionHandler(CatalogUnavailableException.class)
    public ResponseEntity<ApiError> handleCatalogDown(CatalogUnavailableException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Servicio no disponible", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST.value(), "Datos invalidos", "La solicitud tiene campos invalidos o incompletos", request.getRequestURI(), details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud mal formada", "El cuerpo de la solicitud no tiene el formato esperado o usa un valor no permitido", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrio un error inesperado al procesar la solicitud", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), error, message, request.getRequestURI(), List.of()));
    }
}
