package com.pcforge.compatibilidad.service;

/**
 * Quien realiza la peticion, tomado del token JWT. Se usa para verificar la
 * propiedad de las configuraciones: un cliente solo ve las suyas.
 */
public record Caller(String userId, String email, boolean staff) {
}
