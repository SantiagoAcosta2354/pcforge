package com.pcforge.pedidos.config;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.pcforge.pedidos.service.Caller;

/**
 * Traduce el token JWT en los datos del solicitante. El token en crudo se
 * conserva porque se reenvia a los otros microservicios: son ellos los que
 * deben verificar la identidad y la propiedad de sus propios datos.
 */
@Component
public class CallerResolver {

    public Caller from(Jwt jwt, String authorizationHeader) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new Caller(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                roles == null ? List.of() : roles,
                authorizationHeader);
    }
}
