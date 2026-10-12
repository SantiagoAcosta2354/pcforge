package com.pcforge.web.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.pcforge.web.client.CatalogoApiClient;
import com.pcforge.web.security.PcforgeUserDetails;

/**
 * Gestion de existencias. Solo el administrador llega a estas rutas; la
 * restriccion se declara en SecurityConfig y ademas la vuelve a aplicar el
 * microservicio de catalogo, que es quien decide de verdad.
 */
@Controller
@RequestMapping("/inventario")
public class InventarioController {
    private final CatalogoApiClient catalogoApiClient;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", catalogoApiClient.products(null, null, null));
        model.addAttribute("alertas", catalogoApiClient.lowStock());
        return "inventario";
    }

    @PostMapping("/{id}/movimiento")
    public String mover(@PathVariable String id, @RequestParam String movementType, @RequestParam int quantity, @RequestParam(required = false) String reason, @AuthenticationPrincipal PcforgeUserDetails usuario, RedirectAttributes redirectAttributes) {
        var producto = catalogoApiClient.adjustStock(id, movementType, quantity, reason, usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "Existencia de " + producto.sku() + " actualizada a " + producto.stock() + " unidades.");
        return "redirect:/inventario";
    }

    public InventarioController(final CatalogoApiClient catalogoApiClient) {
        this.catalogoApiClient = catalogoApiClient;
    }
}
