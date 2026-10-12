package com.pcforge.catalogo.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.exception.ResourceNotFoundException;
import com.pcforge.catalogo.web.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Respuestas de error uniformes. Se informa que fallo y en que campo, sin
 * exponer excepciones internas ni credenciales de la base de datos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado", ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(HttpStatus.CONFLICT.value(), "Regla de negocio incumplida", ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST.value(), "Datos invalidos", "La solicitud tiene campos invalidos o incompletos", request.getRequestURI(), details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST.value(), "Solicitud mal formada", "El cuerpo de la solicitud no tiene el formato esperado o usa un valor no permitido", request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno", "Ocurrio un error inesperado al procesar la solicitud", request.getRequestURI(), List.of()));
    }
}
