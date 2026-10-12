package com.pcforge.usuarios.exception;

/**
 * Credenciales incorrectas. Se maneja aparte porque la respuesta HTTP correcta
 * es 401 y el mensaje debe ser generico: no conviene revelar si el correo
 * existe o si lo que fallo fue la contrasena.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
