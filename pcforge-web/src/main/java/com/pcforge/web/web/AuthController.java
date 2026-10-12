package com.pcforge.web.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.pcforge.web.client.UsuariosApiClient;

@Controller
public class AuthController {
    private final UsuariosApiClient usuariosApiClient;

    @GetMapping("/login")
    public String login(Model model) {
        if (yaAutenticado()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        if (yaAutenticado()) {
            return "redirect:/";
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String fullName, @RequestParam String email, @RequestParam String password, @RequestParam String passwordConfirm, RedirectAttributes redirectAttributes) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Todos los campos son obligatorios.");
            return "redirect:/registro";
        }
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("mensajeError", "La contrasena debe tener al menos 8 caracteres.");
            return "redirect:/registro";
        }
        if (!password.equals(passwordConfirm)) {
            redirectAttributes.addFlashAttribute("mensajeError", "Las dos contrasenas no coinciden.");
            return "redirect:/registro";
        }
        try {
            usuariosApiClient.register(fullName, email, password);
        } catch (RuntimeException ex) {
            // El mensaje lo redacta el servicio de usuarios; aqui solo se traslada.
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/registro";
        }
        redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta creada. Ya puede iniciar sesion con su correo y contrasena.");
        return "redirect:/login";
    }

    private boolean yaAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public AuthController(final UsuariosApiClient usuariosApiClient) {
        this.usuariosApiClient = usuariosApiClient;
    }
}
