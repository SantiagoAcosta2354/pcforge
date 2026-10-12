package com.pcforge.web.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.pcforge.web.client.CatalogoApiClient;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {
    private final CatalogoApiClient catalogoApiClient;

    @GetMapping
    public String listar(@RequestParam(required = false) String categoria, @RequestParam(required = false) String tipo, @RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("categorias", catalogoApiClient.categories());
        model.addAttribute("productos", catalogoApiClient.products(categoria, tipo, buscar));
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("busqueda", buscar);
        return "catalogo";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        model.addAttribute("producto", catalogoApiClient.product(id));
        return "producto";
    }

    public CatalogoController(final CatalogoApiClient catalogoApiClient) {
        this.catalogoApiClient = catalogoApiClient;
    }
}
