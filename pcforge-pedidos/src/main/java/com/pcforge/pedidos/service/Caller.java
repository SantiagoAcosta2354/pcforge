package com.pcforge.pedidos.service;

import java.util.List;

/**
 * Solicitante tomado del token JWT. Ademas del identificador se conservan los
 * roles, porque las transiciones de estado dependen del perfil: el asesor
 * confirma la cotizacion, el tecnico mueve las etapas del armado.
 */
public record Caller(String userId, String email, String name, List<String> roles, String bearerToken) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /** Administrador o asesor comercial: gestionan la parte comercial. */
    public boolean isCommercialStaff() {
        return hasRole("ADMIN") || hasRole("ASESOR");
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isTechnician() {
        return hasRole("TECNICO");
    }

    /** Quienes pueden consultar los pedidos de todos los clientes. */
    public boolean canSeeAllOrders() {
        return isCommercialStaff() || isTechnician();
    }
}
