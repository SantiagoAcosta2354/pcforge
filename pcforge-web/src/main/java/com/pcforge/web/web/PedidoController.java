package com.pcforge.web.web;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.pcforge.web.client.PedidosApiClient;
import com.pcforge.web.client.UsuariosApiClient;
import com.pcforge.web.security.PcforgeUserDetails;

/**
 * Seguimiento de pedidos. El cliente ve los propios y el personal ve todos;
 * esa decision no se toma aqui, se toma en el microservicio de pedidos, que es
 * el dueÃ±o del dato.
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidosApiClient pedidosApiClient;
    private final UsuariosApiClient usuariosApiClient;

    @GetMapping
    public String listar(@RequestParam(required = false) String estado, @AuthenticationPrincipal PcforgeUserDetails usuario, Model model) {
        model.addAttribute("pedidos", pedidosApiClient.orders(estado, usuario.getAuthorizationHeader()));
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("esPersonal", usuario.isCommercialStaff() || usuario.isTechnician());
        return "pedidos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, @AuthenticationPrincipal PcforgeUserDetails usuario, Model model) {
        model.addAttribute("pedido", pedidosApiClient.order(id, usuario.getAuthorizationHeader()));
        model.addAttribute("puedeCambiarEstado", usuario.isCommercialStaff() || usuario.isTechnician());
        // El asesor necesita la lista de tecnicos para asignar el ensamblaje.
        if (usuario.isCommercialStaff()) {
            List<UsuariosApiClient.UserView> tecnicos = usuariosApiClient.technicians(usuario.getAuthorizationHeader());
            model.addAttribute("tecnicos", tecnicos);
        }
        return "pedido";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable String id, @RequestParam String status, @RequestParam(required = false) String note, @RequestParam(required = false) String technicianId, @RequestParam(required = false) String technicianName, @AuthenticationPrincipal PcforgeUserDetails usuario, RedirectAttributes redirectAttributes) {
        var pedido = pedidosApiClient.changeStatus(id, status, note, technicianId, technicianName, usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "El pedido " + pedido.orderNumber() + " paso al estado " + status + ".");
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/{id}/observacion")
    public String observar(@PathVariable String id, @RequestParam String text, @AuthenticationPrincipal PcforgeUserDetails usuario, RedirectAttributes redirectAttributes) {
        pedidosApiClient.addObservation(id, text, usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "Observacion registrada.");
        return "redirect:/pedidos/" + id;
    }

    public PedidoController(final PedidosApiClient pedidosApiClient, final UsuariosApiClient usuariosApiClient) {
        this.pedidosApiClient = pedidosApiClient;
        this.usuariosApiClient = usuariosApiClient;
    }
}
