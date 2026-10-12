package com.pcforge.web.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * La capa web traduce los roles del token en autoridades y en etiquetas de
 * pantalla. Estas pruebas fijan ese contrato, que es el que decide que ve
 * cada perfil en el menu.
 */
class PcforgeUserDetailsTest {

    @Test
    @DisplayName("Los roles del token se convierten en autoridades con prefijo ROLE_")
    void shouldConvertRolesIntoAuthorities() {
        PcforgeUserDetails usuario = new PcforgeUserDetails(
                "u-1", "asesor@pcforge.co", "Asesor comercial", List.of("ASESOR"), "token");

        Set<String> autoridades = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(autoridades).containsExactly("ROLE_ASESOR");
    }

    @Test
    @DisplayName("El token viaja en el encabezado de autorizacion que se reenvia a los servicios")
    void shouldBuildAuthorizationHeader() {
        PcforgeUserDetails usuario = new PcforgeUserDetails(
                "u-1", "cliente@pcforge.co", "Cliente", List.of("CLIENTE"), "abc.def.ghi");

        assertThat(usuario.getAuthorizationHeader()).isEqualTo("Bearer abc.def.ghi");
    }

    @Test
    @DisplayName("Cada perfil recibe la etiqueta que se muestra en pantalla")
    void shouldLabelEachRole() {
        assertThat(usuarioCon("ADMIN").getRoleLabel()).isEqualTo("Administrador");
        assertThat(usuarioCon("ASESOR").getRoleLabel()).isEqualTo("Asesor comercial");
        assertThat(usuarioCon("TECNICO").getRoleLabel()).isEqualTo("Tecnico de ensamblaje");
        assertThat(usuarioCon("CLIENTE").getRoleLabel()).isEqualTo("Cliente");
    }

    @Test
    @DisplayName("Solo el administrador gestiona inventario; el asesor y el tecnico no")
    void shouldRestrictInventoryManagement() {
        assertThat(usuarioCon("ADMIN").isInventoryManager()).isTrue();
        assertThat(usuarioCon("ASESOR").isInventoryManager()).isFalse();
        assertThat(usuarioCon("TECNICO").isInventoryManager()).isFalse();
        assertThat(usuarioCon("CLIENTE").isInventoryManager()).isFalse();
    }

    @Test
    @DisplayName("El asesor y el administrador son personal comercial, el tecnico no")
    void shouldIdentifyCommercialStaff() {
        assertThat(usuarioCon("ADMIN").isCommercialStaff()).isTrue();
        assertThat(usuarioCon("ASESOR").isCommercialStaff()).isTrue();
        assertThat(usuarioCon("TECNICO").isCommercialStaff()).isFalse();
        assertThat(usuarioCon("CLIENTE").isCommercialStaff()).isFalse();
    }

    @Test
    @DisplayName("Un usuario con varios roles acumula las capacidades de todos")
    void shouldAccumulateCapabilities() {
        PcforgeUserDetails usuario = new PcforgeUserDetails(
                "u-9", "mixto@pcforge.co", "Mixto", List.of("TECNICO", "ASESOR"), "token");

        assertThat(usuario.isTechnician()).isTrue();
        assertThat(usuario.isCommercialStaff()).isTrue();
        assertThat(usuario.isInventoryManager()).isFalse();
    }

    private PcforgeUserDetails usuarioCon(String rol) {
        return new PcforgeUserDetails("u-1", "usuario@pcforge.co", "Usuario", List.of(rol), "token");
    }
}
