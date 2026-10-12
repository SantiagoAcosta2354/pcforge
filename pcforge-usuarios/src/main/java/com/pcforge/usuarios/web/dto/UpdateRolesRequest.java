package com.pcforge.usuarios.web.dto;

import java.util.Set;

import com.pcforge.usuarios.domain.Role;

import jakarta.validation.constraints.NotEmpty;

/** Cambio de roles de una cuenta. Solo lo puede ejecutar un administrador. */
public record UpdateRolesRequest(

        @NotEmpty(message = "Debe indicar al menos un rol") Set<Role> roles) {
}
