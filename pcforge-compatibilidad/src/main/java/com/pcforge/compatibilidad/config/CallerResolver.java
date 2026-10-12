package com.pcforge.compatibilidad.config;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.pcforge.compatibilidad.service.Caller;

/**
 * Traduce el token JWT en los datos del solicitante. Se considera personal a
 * los roles ADMIN y ASESOR, que pueden consultar las configuraciones de
 * cualquier cliente.
 */
@Component
public class CallerResolver {

    public Caller from(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        boolean staff = roles != null && (roles.contains("ADMIN") || roles.contains("ASESOR"));
        return new Caller(jwt.getSubject(), jwt.getClaimAsString("email"), staff);
    }
}
