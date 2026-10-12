package com.pcforge.web.web;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.pcforge.web.client.CatalogoApiClient;

@Controller
public class HomeController {
    private final CatalogoApiClient catalogoApiClient;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("categorias", catalogoApiClient.categories());
        // Unos pocos productos para la portada: si el catalogo esta vacio o el
        // servicio no responde, la pagina igual debe cargar.
        List<CatalogoApiClient.ProductView> destacados = catalogoApiClient.products(null, null, null);
        model.addAttribute("destacados", destacados.stream().limit(6).toList());
        model.addAttribute("totalProductos", destacados.size());
        return "inicio";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/acceso-denegado";
    }

    public HomeController(final CatalogoApiClient catalogoApiClient) {
        this.catalogoApiClient = catalogoApiClient;
    }
}
