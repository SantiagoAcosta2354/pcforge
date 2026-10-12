package com.pcforge.usuarios.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pcforge.usuarios.domain.Role;
import com.pcforge.usuarios.domain.User;
import com.pcforge.usuarios.exception.BusinessRuleException;
import com.pcforge.usuarios.exception.InvalidCredentialsException;
import com.pcforge.usuarios.exception.ResourceNotFoundException;
import com.pcforge.usuarios.repository.UserRepository;
import com.pcforge.usuarios.web.dto.ChangePasswordRequest;
import com.pcforge.usuarios.web.dto.RegisterRequest;
import com.pcforge.usuarios.web.dto.UserResponse;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------------- consultas
    public List<UserResponse> findAll(boolean includeInactive) {
        List<User> users = includeInactive ? userRepository.findAll() : userRepository.findByActiveTrueOrderByFullNameAsc();
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(String id) {
        return UserResponse.from(requireUser(id));
    }

    /**
     * Cuentas activas con un rol determinado (por ejemplo, los tecnicos).
     */
    public List<UserResponse> findByRole(Role role) {
        return userRepository.findByRolesContaining(role).stream().filter(User::isActive).map(UserResponse::from).toList();
    }

    // ---------------------------------------------------------------- comandos
    /**
     * El registro publico siempre crea una cuenta con rol CLIENTE.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Ya existe una cuenta registrada con el correo " + email);
        }
        User user = User.builder().fullName(request.fullName().trim()).email(email).passwordHash(passwordEncoder.encode(request.password())).roles(Set.of(Role.CLIENTE)).active(true).build();
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Verificacion de credenciales. Se lanza el mismo error tanto si el correo
     * no existe como si la contrasena es incorrecta, para no dar pistas sobre
     * que cuentas estan registradas.
     */
    @Transactional
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(normalizeEmail(email)).orElseThrow(() -> new InvalidCredentialsException("Correo o contrasena incorrectos"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Correo o contrasena incorrectos");
        }
        if (!user.canAuthenticate()) {
            throw new BusinessRuleException("La cuenta esta inactiva; contacte al administrador");
        }
        user.setLastLoginAt(Instant.now());
        return userRepository.save(user);
    }

    @Transactional
    public UserResponse updateRoles(String id, Set<Role> roles) {
        User user = requireUser(id);
        user.setRoles(roles);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse changeStatus(String id, boolean active) {
        User user = requireUser(id);
        user.setActive(active);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String id, ChangePasswordRequest request) {
        User user = requireUser(id);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("La contrasena actual no es correcta");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ---------------------------------------------------------------- apoyo
    public User requireUser(String id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe una cuenta con el id " + id));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}
