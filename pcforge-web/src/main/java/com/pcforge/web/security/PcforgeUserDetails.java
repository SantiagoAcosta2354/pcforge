package com.pcforge.web.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Usuario autenticado en la capa web.
 *
 * Ademas de los datos de la cuenta guarda el token JWT que emitio el
 * microservicio de usuarios. Ese token es el que se reenvia en cada llamada a
 * los demas servicios: la capa web no decide por si misma que puede hacer un
 * usuario, solo transporta la credencial que los servicios validan.
 */
public class PcforgeUserDetails implements UserDetails {

    private final String userId;
    private final String email;
    private final String fullName;
    private final List<String> roles;
    private final String token;

    public PcforgeUserDetails(String userId, String email, String fullName, List<String> roles, String token) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles == null ? List.of() : roles;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getToken() {
        return token;
    }

    public String getAuthorizationHeader() {
        return "Bearer " + token;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /** Puede gestionar la parte comercial del negocio. */
    public boolean isCommercialStaff() {
        return isAdmin() || hasRole("ASESOR");
    }

    public boolean isTechnician() {
        return hasRole("TECNICO");
    }

    /** Puede consultar y ajustar inventario. */
    public boolean isInventoryManager() {
        return isAdmin();
    }

    /** Etiqueta legible del perfil, para mostrarla en la interfaz. */
    public String getRoleLabel() {
        if (isAdmin()) {
            return "Administrador";
        }
        if (isCommercialStaff()) {
            return "Asesor comercial";
        }
        if (isTechnician()) {
            return "Tecnico de ensamblaje";
        }
        return "Cliente";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }

    @Override
    public String getPassword() {
        // La contrasena nunca llega a la capa web: la valida el servicio de usuarios.
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
