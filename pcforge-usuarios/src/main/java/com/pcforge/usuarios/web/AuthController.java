package com.pcforge.usuarios.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.pcforge.usuarios.domain.User;
import com.pcforge.usuarios.service.TokenService;
import com.pcforge.usuarios.service.UserService;
import com.pcforge.usuarios.web.dto.LoginRequest;
import com.pcforge.usuarios.web.dto.RegisterRequest;
import com.pcforge.usuarios.web.dto.TokenResponse;
import com.pcforge.usuarios.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro e inicio de sesion")
public class AuthController {
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una cuenta nueva con rol de cliente")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesion y devuelve el token JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        return tokenService.issue(user);
    }

    public AuthController(final UserService userService, final TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }
}
