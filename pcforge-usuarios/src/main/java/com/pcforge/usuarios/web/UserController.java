package com.pcforge.usuarios.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.pcforge.usuarios.domain.Role;
import com.pcforge.usuarios.service.UserService;
import com.pcforge.usuarios.web.dto.ChangePasswordRequest;
import com.pcforge.usuarios.web.dto.UpdateRolesRequest;
import com.pcforge.usuarios.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Cuentas y roles")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Devuelve la cuenta del usuario autenticado")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.findById(jwt.getSubject());
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambia la contrasena del usuario autenticado")
    public void changeMyPassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(jwt.getSubject(), request);
    }

    /**
     * Tecnicos disponibles para asignar un ensamblaje. Lo consulta el asesor
     * comercial al iniciar un pedido, por eso no exige rol de administrador.
     */
    @GetMapping("/technicians")
    @Operation(summary = "Lista los tecnicos de ensamblaje activos")
    public List<UserResponse> technicians() {
        return userService.findByRole(Role.TECNICO);
    }

    @GetMapping
    @Operation(summary = "Lista las cuentas registradas (solo administrador)")
    public List<UserResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return userService.findAll(includeInactive);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una cuenta por su identificador (solo administrador)")
    public UserResponse findById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Asigna los roles de una cuenta (solo administrador)")
    public UserResponse updateRoles(@PathVariable String id, @Valid @RequestBody UpdateRolesRequest request) {
        return userService.updateRoles(id, request.roles());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activa o desactiva una cuenta (solo administrador)")
    public UserResponse changeStatus(@PathVariable String id, @RequestParam boolean active) {
        return userService.changeStatus(id, active);
    }

    public UserController(final UserService userService) {
        this.userService = userService;
    }
}
