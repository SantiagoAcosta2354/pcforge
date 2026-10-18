package com.pcforge.web.web;

import java.math.BigDecimal;
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

import com.pcforge.web.client.PreciosApiClient;
import com.pcforge.web.exception.BusinessMessageException;
import com.pcforge.web.security.PcforgeUserDetails;

/**
 * Pantalla del analista de precios y margenes.
 *
 * La politica se edita aqui, pero el precio del producto no: esa es la
 * frontera del rol. El costo se muestra porque es la base del calculo, y su
 * registro sigue siendo del administrador.
 */
@Controller
@RequestMapping("/precios")
public class PreciosController {

    private final PreciosApiClient preciosApiClient;

    @GetMapping
    public String panel(@RequestParam(defaultValue = "true") boolean soloAlertas, Model model,
            @AuthenticationPrincipal PcforgeUserDetails usuario) {
        List<PreciosApiClient.Margin> margenes = preciosApiClient.margins(soloAlertas,
                usuario.getAuthorizationHeader());
        model.addAttribute("politicas", preciosApiClient.policies(usuario.getAuthorizationHeader()));
        model.addAttribute("margenes", margenes);
        model.addAttribute("soloAlertas", soloAlertas);
        model.addAttribute("alertas", margenes.stream().filter(m -> !"CONFORME".equals(m.status())).count());
        return "precios";
    }

    @PostMapping("/politica/{categoryId}")
    public String actualizarPolitica(@PathVariable String categoryId,
            @RequestParam BigDecimal minimumMargin,
            @RequestParam BigDecimal discountThreshold,
            @AuthenticationPrincipal PcforgeUserDetails usuario,
            RedirectAttributes redirectAttributes) {

        if (minimumMargin.compareTo(BigDecimal.ZERO) < 0 || minimumMargin.compareTo(new BigDecimal("90")) > 0) {
            throw new BusinessMessageException("El margen minimo debe estar entre 0 y 90 por ciento.");
        }
        if (discountThreshold.compareTo(BigDecimal.ZERO) < 0
                || discountThreshold.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessMessageException("El umbral de descuento debe estar entre 0 y 100 por ciento.");
        }

        PreciosApiClient.Policy politica = preciosApiClient.updatePolicy(categoryId, minimumMargin, discountThreshold,
                usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito",
                "Politica de " + politica.categoryName() + " actualizada: margen minimo "
                        + politica.minimumMargin() + " %, descuento sin aprobacion " + politica.discountThreshold()
                        + " %.");
        return "redirect:/precios";
    }

    public PreciosController(final PreciosApiClient preciosApiClient) {
        this.preciosApiClient = preciosApiClient;
    }
}
