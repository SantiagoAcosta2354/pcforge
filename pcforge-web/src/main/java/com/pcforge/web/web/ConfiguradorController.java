package com.pcforge.web.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.pcforge.web.client.CatalogoApiClient;
import com.pcforge.web.client.CompatibilidadApiClient;
import com.pcforge.web.client.PedidosApiClient;
import com.pcforge.web.exception.NotFoundInViewException;
import com.pcforge.web.security.PcforgeUserDetails;
import jakarta.servlet.http.HttpSession;

/**
 * Armado de equipos. La configuracion en curso se guarda en la sesion, de modo
 * que el cliente pueda moverse por el catalogo agregando componentes sin
 * perder lo que llevaba.
 */
@Controller
@RequestMapping("/armar")
public class ConfiguradorController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfiguradorController.class);
    /**
     * Nombre del atributo de sesion que guarda la configuracion en curso.
     */
    static final String CONFIGURACION_EN_SESION = "configuracionEnCurso";
    private final CatalogoApiClient catalogoApiClient;
    private final CompatibilidadApiClient compatibilidadApiClient;
    private final PedidosApiClient pedidosApiClient;

    @GetMapping
    public String ver(@AuthenticationPrincipal PcforgeUserDetails usuario, HttpSession session, Model model) {
        String configuracionId = (String) session.getAttribute(CONFIGURACION_EN_SESION);
        if (configuracionId != null) {
            try {
                model.addAttribute("configuracion", compatibilidadApiClient.configuration(configuracionId, usuario.getAuthorizationHeader()));
            } catch (NotFoundInViewException ex) {
                // La configuracion ya no existe; se empieza de nuevo sin romper la pagina.
                session.removeAttribute(CONFIGURACION_EN_SESION);
            }
        }
        model.addAttribute("categorias", catalogoApiClient.categories());
        model.addAttribute("productos", catalogoApiClient.products(null, null, null));
        return "configurador";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam String productId, @RequestParam(defaultValue = "1") int cantidad, @AuthenticationPrincipal PcforgeUserDetails usuario, HttpSession session) {
        log.info("Agregar componente: productId=\'{}\' cantidad={} usuario={}", productId, cantidad, usuario == null ? "anonimo" : usuario.getEmail());
        String configuracionId = (String) session.getAttribute(CONFIGURACION_EN_SESION);
        if (configuracionId == null) {
            configuracionId = compatibilidadApiClient.create("Equipo de " + usuario.getFullName(), usuario.getAuthorizationHeader()).id();
            session.setAttribute(CONFIGURACION_EN_SESION, configuracionId);
        }
        compatibilidadApiClient.addItem(configuracionId, productId, Math.max(cantidad, 1), usuario.getAuthorizationHeader());
        return "redirect:/armar";
    }

    @PostMapping("/quitar")
    public String quitar(@RequestParam String itemId, @AuthenticationPrincipal PcforgeUserDetails usuario, HttpSession session) {
        String configuracionId = (String) session.getAttribute(CONFIGURACION_EN_SESION);
        if (configuracionId != null) {
            compatibilidadApiClient.removeItem(configuracionId, itemId, usuario.getAuthorizationHeader());
        }
        return "redirect:/armar";
    }

    @PostMapping("/validar")
    public String validar(@AuthenticationPrincipal PcforgeUserDetails usuario, HttpSession session, RedirectAttributes redirectAttributes) {
        String configuracionId = (String) session.getAttribute(CONFIGURACION_EN_SESION);
        if (configuracionId == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Todavia no ha agregado componentes al equipo.");
            return "redirect:/armar";
        }
        // El resultado viaja en un atributo flash para que sobreviva a la redireccion.
        redirectAttributes.addFlashAttribute("validacion", compatibilidadApiClient.validate(configuracionId, usuario.getAuthorizationHeader()));
        return "redirect:/armar";
    }

    @PostMapping("/vaciar")
    public String vaciar(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(CONFIGURACION_EN_SESION);
        redirectAttributes.addFlashAttribute("mensajeExito", "Se descarto el equipo que estaba armando.");
        return "redirect:/armar";
    }

    @PostMapping("/pedido")
    public String registrarPedido(@RequestParam(required = false) String observacion, @AuthenticationPrincipal PcforgeUserDetails usuario, HttpSession session, RedirectAttributes redirectAttributes) {
        String configuracionId = (String) session.getAttribute(CONFIGURACION_EN_SESION);
        if (configuracionId == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Todavia no ha agregado componentes al equipo.");
            return "redirect:/armar";
        }
        var pedido = pedidosApiClient.create(configuracionId, observacion, usuario.getAuthorizationHeader());
        session.removeAttribute(CONFIGURACION_EN_SESION);
        redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud " + pedido.orderNumber() + " registrada. El asesor comercial la revisara.");
        return "redirect:/pedidos/" + pedido.id();
    }

    public ConfiguradorController(final CatalogoApiClient catalogoApiClient, final CompatibilidadApiClient compatibilidadApiClient, final PedidosApiClient pedidosApiClient) {
        this.catalogoApiClient = catalogoApiClient;
        this.compatibilidadApiClient = compatibilidadApiClient;
        this.pedidosApiClient = pedidosApiClient;
    }
}
