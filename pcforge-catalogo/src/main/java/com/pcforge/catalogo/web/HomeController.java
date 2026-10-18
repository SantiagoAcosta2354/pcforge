package com.pcforge.catalogo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Entrada del servicio. Un microservicio no tiene pagina de inicio, pero
 * tampoco conviene que quien escriba la URL base reciba un 401 en JSON y crea
 * que el servicio esta caido o mal configurado. La raiz redirige a la
 * documentacion, que es la unica pantalla que este servicio puede ofrecer.
 *
 * El prefijo /api/v1 sigue siendo la unica via de acceso a los datos.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui.html";
    }
}
