package com.pcforge.usuarios.web.dto;

import java.time.Instant;
import java.util.Set;

import com.pcforge.usuarios.domain.Role;
import com.pcforge.usuarios.domain.User;

/**
 * Datos publicos de una cuenta. El hash de la contrasena no forma parte de
 * esta representacion: nunca sale del servicio.
 */
public record UserResponse(
        String id,
        String fullName,
        String email,
        Set<Role> roles,
        boolean active,
        Instant lastLoginAt,
        Instant createdAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRoles(),
                u.isActive(), u.getLastLoginAt(), u.getCreatedAt());
    }
}
