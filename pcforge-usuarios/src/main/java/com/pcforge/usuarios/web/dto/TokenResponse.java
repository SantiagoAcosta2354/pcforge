package com.pcforge.usuarios.web.dto;

/** Respuesta del inicio de sesion: el token y los datos del usuario. */
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user) {

    public static TokenResponse bearer(String token, long expiresInSeconds, UserResponse user) {
        return new TokenResponse(token, "Bearer", expiresInSeconds, user);
    }
}
