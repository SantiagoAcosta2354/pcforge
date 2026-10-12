package com.pcforge.web.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pcforge.web.client.CatalogoApiClient;
import com.pcforge.web.client.CatalogoApiClient.CategoryView;
import com.pcforge.web.client.CatalogoApiClient.ProductView;
import com.pcforge.web.security.PcforgeUserDetails;
import com.pcforge.web.web.form.CategoriaForm;
import com.pcforge.web.web.form.ProductoForm;

/**
 * Gestion del catalogo desde la propia aplicacion: altas, modificaciones y
 * activacion o desactivacion de productos y categorias.
 *
 * Hasta ahora estas operaciones solo existian en el API, asi que habia que
 * hacerlas con un cliente HTTP. Aqui se exponen como pantallas.
 *
 * La restriccion por perfil se declara en SecurityConfig y la vuelve a aplicar
 * el microservicio de catalogo: la capa web no decide permisos, solo reenvia el
 * token de quien esta usando la aplicacion.
 */
@Controller
@RequestMapping("/admin")
public class AdminCatalogoController {

    private final CatalogoApiClient catalogoApiClient;

    @GetMapping
    public String inicio() {
        return "redirect:/admin/productos";
    }

    // ------------------------------------------------------------------
    // Productos
    // ------------------------------------------------------------------

    @GetMapping("/productos")
    public String productos(@RequestParam(required = false) String q, @RequestParam(required = false) String categoria, @RequestParam(required = false) String tipo, @RequestParam(required = false) String estado, Model model) {
        List<ProductView> todos = catalogoApiClient.adminProducts(null, null, null);
        List<ProductView> filtrados = new ArrayList<>();
        String texto = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        for (ProductView p : todos) {
            if (!texto.isEmpty() && !p.name().toLowerCase(Locale.ROOT).contains(texto) && !p.sku().toLowerCase(Locale.ROOT).contains(texto)) {
                continue;
            }
            if (categoria != null && !categoria.isBlank() && !categoria.equals(p.categoryId())) {
                continue;
            }
            if (tipo != null && !tipo.isBlank() && !tipo.equals(p.componentType())) {
                continue;
            }
            if ("activos".equals(estado) && !p.active()) {
                continue;
            }
            if ("inactivos".equals(estado) && p.active()) {
                continue;
            }
            filtrados.add(p);
        }
        model.addAttribute("productos", filtrados);
        model.addAttribute("totalCatalogo", todos.size());
        model.addAttribute("categorias", catalogoApiClient.adminCategories());
        model.addAttribute("filtroQ", q);
        model.addAttribute("filtroCategoria", categoria);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("filtroEstado", estado);
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevo(Model model) {
        preparar(model, new ProductoForm());
        return "admin/producto-form";
    }

    @PostMapping("/productos")
    public String crear(@ModelAttribute ProductoForm form, @AuthenticationPrincipal PcforgeUserDetails usuario, Model model, RedirectAttributes redirectAttributes) {
        String problema = validar(form);
        if (problema != null) {
            model.addAttribute("mensajeError", problema);
            preparar(model, form);
            return "admin/producto-form";
        }
        ProductView creado = catalogoApiClient.createProduct(payload(form), usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "Producto " + creado.sku() + " creado.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/{id}/editar")
    public String editar(@PathVariable String id, Model model) {
        ProductView producto = catalogoApiClient.product(id);
        if (producto == null) {
            model.addAttribute("mensajeError", "El producto solicitado no existe.");
            return "redirect:/admin/productos";
        }
        preparar(model, aFormulario(producto));
        return "admin/producto-form";
    }

    @PostMapping("/productos/{id}")
    public String actualizar(@PathVariable String id, @ModelAttribute ProductoForm form, @AuthenticationPrincipal PcforgeUserDetails usuario, Model model, RedirectAttributes redirectAttributes) {
        form.setId(id);
        String problema = validar(form);
        if (problema != null) {
            model.addAttribute("mensajeError", problema);
            preparar(model, form);
            return "admin/producto-form";
        }
        ProductView actualizado = catalogoApiClient.updateProduct(id, payload(form), usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "Producto " + actualizado.sku() + " actualizado.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/estado")
    public String estadoProducto(@PathVariable String id, @RequestParam boolean active, @AuthenticationPrincipal PcforgeUserDetails usuario, RedirectAttributes redirectAttributes) {
        ProductView producto = catalogoApiClient.setProductStatus(id, active, usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "El producto " + producto.sku() + (active ? " quedo activo." : " quedo desactivado."));
        return "redirect:/admin/productos";
    }

    // ------------------------------------------------------------------
    // Categorias
    // ------------------------------------------------------------------

    @GetMapping("/categorias")
    public String categorias(Model model) {
        List<CategoryView> categorias = catalogoApiClient.adminCategories();
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (CategoryView c : categorias) {
            conteo.put(c.id(), 0);
        }
        for (ProductView p : catalogoApiClient.adminProducts(null, null, null)) {
            conteo.computeIfPresent(p.categoryId(), (clave, actual) -> actual + 1);
        }
        model.addAttribute("categorias", categorias);
        model.addAttribute("productosPorCategoria", conteo);
        return "admin/categorias";
    }

    @GetMapping("/categorias/nueva")
    public String nuevaCategoria(Model model) {
        prepararCategoria(model, new CategoriaForm());
        return "admin/categoria-form";
    }

    @PostMapping("/categorias")
    public String crearCategoria(@ModelAttribute CategoriaForm form, @AuthenticationPrincipal PcforgeUserDetails usuario, Model model, RedirectAttributes redirectAttributes) {
        String problema = validarCategoria(form);
        if (problema != null) {
            model.addAttribute("mensajeError", problema);
            prepararCategoria(model, form);
            return "admin/categoria-form";
        }
        CategoryView creada = catalogoApiClient.createCategory(payloadCategoria(form), usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "Categoria " + creada.name() + " creada.");
        return "redirect:/admin/categorias";
    }

    @GetMapping("/categorias/{id}/editar")
    public String editarCategoria(@PathVariable String id, Model model) {
        for (CategoryView c : catalogoApiClient.adminCategories()) {
            if (c.id().equals(id)) {
                CategoriaForm form = new CategoriaForm();
                form.setId(c.id());
                form.setName(c.name());
                form.setSlug(c.slug());
                form.setDescription(c.description());
                form.setActive(c.active());
                prepararCategoria(model, form);
                return "admin/categoria-form";
            }
        }
        model.addAttribute("mensajeError", "La categoria solicitada no existe.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/categorias/{id}")
    public String actualizarCategoria(@PathVariable String id, @ModelAttribute CategoriaForm form, @AuthenticationPrincipal PcforgeUserDetails usuario, Model model, RedirectAttributes redirectAttributes) {
        form.setId(id);
        String problema = validarCategoria(form);
        if (problema != null) {
            model.addAttribute("mensajeError", problema);
            prepararCategoria(model, form);
            return "admin/categoria-form";
        }
        CategoryView actualizada = catalogoApiClient.updateCategory(id, payloadCategoria(form), usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "Categoria " + actualizada.name() + " actualizada.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/categorias/{id}/estado")
    public String estadoCategoria(@PathVariable String id, @RequestParam boolean active, @AuthenticationPrincipal PcforgeUserDetails usuario, RedirectAttributes redirectAttributes) {
        CategoryView categoria = catalogoApiClient.setCategoryStatus(id, active, usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", "La categoria " + categoria.name() + (active ? " quedo activa." : " quedo desactivada."));
        return "redirect:/admin/categorias";
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private void preparar(Model model, ProductoForm form) {
        model.addAttribute("form", form);
        model.addAttribute("categorias", catalogoApiClient.adminCategories());
    }

    private void prepararCategoria(Model model, CategoriaForm form) {
        model.addAttribute("form", form);
    }

    /** Pasa lo que devuelve el servicio al formulario, para poder editarlo. */
    private ProductoForm aFormulario(ProductView producto) {
        ProductoForm form = new ProductoForm();
        form.setId(producto.id());
        form.setSku(producto.sku());
        form.setName(producto.name());
        form.setBrand(producto.brand());
        form.setCategoryId(producto.categoryId());
        form.setComponentType(producto.componentType());
        form.setDescription(producto.description());
        form.setPrice(producto.price());
        form.setStock(producto.stock());
        form.setMinimumStock(producto.minimumStock());
        form.setActive(producto.active());
        if (producto.specs() != null) {
            var specs = producto.specs();
            form.setSocket(specs.socket());
            form.setRamType(specs.ramType());
            if (specs.ramTypeSupported() != null) {
                form.setRamTypeSupported(String.join(", ", specs.ramTypeSupported()));
            }
            form.setEstimatedPowerW(specs.estimatedPowerW());
            form.setCapacityW(specs.capacityW());
            form.setFormFactor(specs.formFactor());
            if (specs.extra() != null) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> e : specs.extra().entrySet()) {
                    sb.append(e.getKey()).append('=').append(e.getValue()).append(System.lineSeparator());
                }
                form.setExtra(sb.toString());
            }
        }
        return form;
    }

    private Map<String, Object> payload(ProductoForm form) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sku", form.getSku().trim());
        body.put("name", form.getName().trim());
        body.put("brand", texto(form.getBrand()));
        body.put("categoryId", form.getCategoryId());
        body.put("componentType", form.getComponentType());
        body.put("description", texto(form.getDescription()));
        body.put("price", form.getPrice() == null ? BigDecimal.ZERO : form.getPrice());
        body.put("stock", form.getStock() == null ? 0 : form.getStock());
        body.put("minimumStock", form.getMinimumStock() == null ? 0 : form.getMinimumStock());
        body.put("active", form.isActive());
        Map<String, Object> specs = specs(form);
        if (!specs.isEmpty()) {
            body.put("specs", specs);
        }
        return body;
    }

    private Map<String, Object> specs(ProductoForm form) {
        Map<String, Object> specs = new LinkedHashMap<>();
        if (hayTexto(form.getSocket())) {
            specs.put("socket", form.getSocket().trim());
        }
        if (hayTexto(form.getRamTypeSupported())) {
            List<String> tipos = new ArrayList<>();
            for (String parte : form.getRamTypeSupported().split(",")) {
                if (!parte.isBlank()) {
                    tipos.add(parte.trim().toUpperCase(Locale.ROOT));
                }
            }
            if (!tipos.isEmpty()) {
                specs.put("ramTypeSupported", tipos);
            }
        }
        if (hayTexto(form.getRamType())) {
            specs.put("ramType", form.getRamType().trim().toUpperCase(Locale.ROOT));
        }
        if (form.getEstimatedPowerW() != null) {
            specs.put("estimatedPowerW", form.getEstimatedPowerW());
        }
        if (form.getCapacityW() != null) {
            specs.put("capacityW", form.getCapacityW());
        }
        if (hayTexto(form.getFormFactor())) {
            specs.put("formFactor", form.getFormFactor().trim());
        }
        Map<String, String> extra = extra(form.getExtra());
        if (!extra.isEmpty()) {
            specs.put("extra", extra);
        }
        return specs;
    }

    /**
     * Lee la ficha informativa libre.
     *
     * El motivo de validar aqui los puntos es concreto: Spring Data MongoDB no
     * admite puntos en los nombres de campo, asi que una clave como "Ranuras M.2"
     * hace fallar el guardado con un error 500 sin explicacion. Es mejor
     * detenerla en el formulario y decir que pasa.
     */
    private Map<String, String> extra(String texto) {
        Map<String, String> extra = new LinkedHashMap<>();
        if (texto == null || texto.isBlank()) {
            return extra;
        }
        for (String linea : texto.split("\\R")) {
            String limpia = linea.trim();
            if (limpia.isEmpty()) {
                continue;
            }
            int corte = limpia.indexOf('=');
            if (corte <= 0) {
                throw new IllegalArgumentException("La linea \"" + limpia + "\" no tiene el formato clave=valor.");
            }
            String clave = limpia.substring(0, corte).trim();
            String valor = limpia.substring(corte + 1).trim();
            if (clave.isEmpty()) {
                throw new IllegalArgumentException("Hay una linea de la ficha tecnica sin clave.");
            }
            if (clave.contains(".")) {
                throw new IllegalArgumentException("La clave \"" + clave + "\" no puede contener puntos porque la base de datos no los admite en nombres de campo. Use por ejemplo \"Ranuras M2\".");
            }
            if (extra.containsKey(clave)) {
                throw new IllegalArgumentException("La clave \"" + clave + "\" aparece repetida en la ficha tecnica.");
            }
            extra.put(clave, valor);
        }
        return extra;
    }

    private Map<String, Object> payloadCategoria(CategoriaForm form) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", form.getName().trim());
        body.put("slug", form.getSlug().trim().toLowerCase(Locale.ROOT).replace(' ', '-'));
        body.put("description", texto(form.getDescription()));
        body.put("active", form.isActive());
        return body;
    }

    private String validar(ProductoForm form) {
        if (!hayTexto(form.getSku())) {
            return "El SKU es obligatorio.";
        }
        if (form.getSku().trim().length() > 40) {
            return "El SKU no puede superar 40 caracteres.";
        }
        if (!hayTexto(form.getName())) {
            return "El nombre del producto es obligatorio.";
        }
        if (form.getName().trim().length() > 140) {
            return "El nombre no puede superar 140 caracteres.";
        }
        if (!hayTexto(form.getCategoryId())) {
            return "Debe elegir una categoria.";
        }
        if (!hayTexto(form.getComponentType())) {
            return "Debe elegir el tipo de componente.";
        }
        if (hayTexto(form.getDescription()) && form.getDescription().trim().length() > 600) {
            return "La descripcion no puede superar 600 caracteres.";
        }
        if (form.getPrice() == null) {
            return "El precio es obligatorio.";
        }
        if (form.getPrice().signum() < 0) {
            return "El precio no puede ser negativo.";
        }
        if (form.getStock() != null && form.getStock() < 0) {
            return "La existencia no puede ser negativa.";
        }
        if (form.getMinimumStock() != null && form.getMinimumStock() < 0) {
            return "El minimo de existencia no puede ser negativo.";
        }
        try {
            extra(form.getExtra());
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return null;
    }

    private String validarCategoria(CategoriaForm form) {
        if (!hayTexto(form.getName())) {
            return "El nombre de la categoria es obligatorio.";
        }
        if (form.getName().trim().length() > 80) {
            return "El nombre no puede superar 80 caracteres.";
        }
        if (!hayTexto(form.getSlug())) {
            return "El identificador es obligatorio.";
        }
        if (form.getSlug().trim().length() > 80) {
            return "El identificador no puede superar 80 caracteres.";
        }
        if (hayTexto(form.getDescription()) && form.getDescription().trim().length() > 300) {
            return "La descripcion no puede superar 300 caracteres.";
        }
        return null;
    }

    private static boolean hayTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public AdminCatalogoController(final CatalogoApiClient catalogoApiClient) {
        this.catalogoApiClient = catalogoApiClient;
    }
}
