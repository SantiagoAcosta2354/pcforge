package com.pcforge.web.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.pcforge.web.client.UsuariosApiClient;
import com.pcforge.web.client.UsuariosApiClient.TokenView;
import com.pcforge.web.client.UsuariosApiClient.UserView;
import com.pcforge.web.exception.BusinessMessageException;
import com.pcforge.web.exception.DownstreamUnavailableException;

class PcforgeAuthenticationProviderTest {

    private UsuariosApiClient usuariosApiClient;
    private PcforgeAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        usuariosApiClient = Mockito.mock(UsuariosApiClient.class);
        provider = new PcforgeAuthenticationProvider(usuariosApiClient);
    }

    @Test
    @DisplayName("Con credenciales validas se conserva el token para reenviarlo a los servicios")
    void shouldKeepTokenOnSuccessfulLogin() {
        when(usuariosApiClient.login("cliente@pcforge.co", "Demo1234*")).thenReturn(new TokenView(
                "token-abc", "Bearer", 28800,
                new UserView("u-1", "Cliente de prueba", "cliente@pcforge.co", List.of("CLIENTE"),
                        true, null, null)));

        Authentication resultado = provider.authenticate(
                new UsernamePasswordAuthenticationToken("cliente@pcforge.co", "Demo1234*"));

        assertThat(resultado.isAuthenticated()).isTrue();
        PcforgeUserDetails principal = (PcforgeUserDetails) resultado.getPrincipal();
        assertThat(principal.getToken()).isEqualTo("token-abc");
        assertThat(principal.getAuthorizationHeader()).isEqualTo("Bearer token-abc");
        assertThat(principal.getRoles()).containsExactly("CLIENTE");
    }

    @Test
    @DisplayName("Credenciales incorrectas se rechazan con un mensaje generico")
    void shouldRejectBadCredentials() {
        when(usuariosApiClient.login(anyString(), anyString()))
                .thenThrow(new BusinessMessageException("Correo o contrasena incorrectos"));

        assertThatThrownBy(() -> provider.authenticate(
                new UsernamePasswordAuthenticationToken("nadie@pcforge.co", "clave-mala")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Correo o contrasena incorrectos");
    }

    @Test
    @DisplayName("Si el servicio de usuarios no responde se informa sin decir que la clave es incorrecta")
    void shouldInformWhenServiceIsDown() {
        when(usuariosApiClient.login(anyString(), anyString()))
                .thenThrow(new DownstreamUnavailableException("El servicio de usuarios no responde"));

        assertThatThrownBy(() -> provider.authenticate(
                new UsernamePasswordAuthenticationToken("cliente@pcforge.co", "Demo1234*")))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("no esta disponible");
    }

    @Test
    @DisplayName("Solo se atienden autenticaciones por usuario y contrasena")
    void shouldSupportUsernamePasswordAuthentication() {
        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
    }
}
