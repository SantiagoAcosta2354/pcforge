package com.pcforge.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.pcforge.usuarios.domain.Role;
import com.pcforge.usuarios.domain.User;
import com.pcforge.usuarios.web.dto.TokenResponse;

/**
 * El token es el contrato entre este servicio y los demas. Estas pruebas
 * verifican que se firma, que se puede volver a leer con el mismo secreto y
 * que lleva los claims de los que dependen los otros microservicios.
 */
class TokenServiceTest {

    private static final String SECRET = "clave-de-prueba-suficientemente-larga-para-hs256-2026";

    private JwtDecoder decoder;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        tokenService = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(key)));
        ReflectionTestUtils.setField(tokenService, "issuer", "pcforge-usuarios");
        ReflectionTestUtils.setField(tokenService, "expirationMinutes", 60L);
    }

    @Test
    @DisplayName("El token lleva el id del usuario, su correo y sus roles")
    void shouldIssueTokenWithSubjectEmailAndRoles() {
        User user = User.builder()
                .id("u-123").email("asesor@pcforge.co").fullName("Asesor comercial")
                .roles(Set.of(Role.ADMIN, Role.ASESOR)).active(true).build();

        TokenResponse response = tokenService.issue(user);
        Jwt jwt = decoder.decode(response.accessToken());

        assertThat(jwt.getSubject()).isEqualTo("u-123");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("asesor@pcforge.co");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Asesor comercial");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN", "ASESOR");
        // Se lee el claim en crudo: getIssuer() exigiria que fuera una URL.
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("pcforge-usuarios");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600);
    }

    @Test
    @DisplayName("El token incluye fecha de expiracion")
    void shouldSetExpiration() {
        User user = User.builder().id("u-1").email("a@pcforge.co").fullName("A")
                .roles(Set.of(Role.CLIENTE)).active(true).build();

        Jwt jwt = decoder.decode(tokenService.issue(user).accessToken());

        assertThat(jwt.getExpiresAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }
}
