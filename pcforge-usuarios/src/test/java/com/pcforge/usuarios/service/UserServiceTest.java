package com.pcforge.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pcforge.usuarios.domain.Role;
import com.pcforge.usuarios.domain.User;
import com.pcforge.usuarios.exception.BusinessRuleException;
import com.pcforge.usuarios.exception.InvalidCredentialsException;
import com.pcforge.usuarios.repository.UserRepository;
import com.pcforge.usuarios.web.dto.RegisterRequest;
import com.pcforge.usuarios.web.dto.UserResponse;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("El registro guarda la contrasena cifrada y asigna solo el rol CLIENTE")
    void shouldRegisterWithClientRoleAndHashedPassword() {
        when(userRepository.existsByEmail("ana@pcforge.co")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(
                new RegisterRequest("Ana Perez", "Ana@PCForge.co", "ClaveSegura123"));

        assertThat(response.email()).isEqualTo("ana@pcforge.co");
        assertThat(response.roles()).containsExactly(Role.CLIENTE);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("ClaveSegura123");
        assertThat(passwordEncoder.matches("ClaveSegura123", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("No se permite registrar dos cuentas con el mismo correo")
    void shouldRejectDuplicatedEmail() {
        when(userRepository.existsByEmail("ana@pcforge.co")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(
                new RegisterRequest("Ana", "ana@pcforge.co", "ClaveSegura123")))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Una contrasena incorrecta no permite autenticarse")
    void shouldRejectWrongPassword() {
        User stored = User.builder().id("u-1").email("ana@pcforge.co").fullName("Ana")
                .passwordHash(passwordEncoder.encode("ClaveCorrecta1")).roles(Set.of(Role.CLIENTE))
                .active(true).build();
        when(userRepository.findByEmail("ana@pcforge.co")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> userService.authenticate("ana@pcforge.co", "OtraClave123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Correo o contrasena incorrectos");
    }

    @Test
    @DisplayName("Un correo inexistente devuelve el mismo mensaje que una contrasena incorrecta")
    void shouldNotRevealWhetherEmailExists() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("nadie@pcforge.co", "Clave123456"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Correo o contrasena incorrectos");
    }

    @Test
    @DisplayName("Una cuenta inactiva no puede iniciar sesion aunque la clave sea correcta")
    void shouldRejectInactiveAccount() {
        User stored = User.builder().id("u-2").email("inactivo@pcforge.co").fullName("Inactivo")
                .passwordHash(passwordEncoder.encode("ClaveCorrecta1")).roles(Set.of(Role.CLIENTE))
                .active(false).build();
        when(userRepository.findByEmail("inactivo@pcforge.co")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> userService.authenticate("inactivo@pcforge.co", "ClaveCorrecta1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    @DisplayName("El cambio de contrasena exige la contrasena actual correcta")
    void shouldRequireCurrentPasswordToChangeIt() {
        User stored = User.builder().id("u-3").email("ana@pcforge.co").fullName("Ana")
                .passwordHash(passwordEncoder.encode("ClaveCorrecta1")).roles(Set.of(Role.CLIENTE))
                .active(true).build();
        when(userRepository.findById("u-3")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> userService.changePassword("u-3",
                new com.pcforge.usuarios.web.dto.ChangePasswordRequest("ClaveEquivocada", "NuevaClave123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
