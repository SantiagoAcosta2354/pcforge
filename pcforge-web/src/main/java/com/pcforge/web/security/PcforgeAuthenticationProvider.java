package com.pcforge.web.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import com.pcforge.web.client.UsuariosApiClient;
import com.pcforge.web.client.UsuariosApiClient.TokenView;
import com.pcforge.web.exception.DownstreamUnavailableException;

/**
 * Autenticacion contra el microservicio de usuarios.
 *
 * La capa web no guarda contrasenas ni las compara: envia las credenciales al
 * servicio que es dueÃ±o de las cuentas y, si son validas, conserva el token
 * que este devuelve. Esa separacion es la razon por la que aqui no hay ninguna
 * base de datos de usuarios.
 */
@Component
public class PcforgeAuthenticationProvider implements AuthenticationProvider {
    private final UsuariosApiClient usuariosApiClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = String.valueOf(authentication.getPrincipal());
        String password = String.valueOf(authentication.getCredentials());
        TokenView token;
        try {
            token = usuariosApiClient.login(email, password);
        } catch (DownstreamUnavailableException ex) {
            throw new DisabledException("El servicio de usuarios no esta disponible en este momento. Intente de nuevo en unos segundos.");
        } catch (RuntimeException ex) {
            // Credenciales incorrectas o cuenta inactiva: el mensaje se mantiene
            // generico para no revelar que cuentas estan registradas.
            throw new BadCredentialsException("Correo o contrasena incorrectos");
        }
        PcforgeUserDetails principal = new PcforgeUserDetails(token.user().id(), token.user().email(), token.user().fullName(), token.user().roles(), token.accessToken());
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public PcforgeAuthenticationProvider(final UsuariosApiClient usuariosApiClient) {
        this.usuariosApiClient = usuariosApiClient;
    }
}
