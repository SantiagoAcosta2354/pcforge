package com.pcforge.web.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pcforge.web.client.CalidadApiClient;
import com.pcforge.web.exception.BusinessMessageException;
import com.pcforge.web.security.PcforgeUserDetails;

/**
 * Pantalla del curador de datos tecnicos.
 *
 * Solo muestra y envia: quien decide si el dato es suficiente es el servicio
 * de catalogo, que responde 409 cuando se intenta declarar completa una ficha
 * sin atributos obligatorios o sin fuente registrada. Aqui ese rechazo se
 * presenta como un aviso, no como un error tecnico.
 */
@Controller
@RequestMapping("/calidad")
public class CalidadController {

    /** Nombres legibles de los atributos que viajan como clave tecnica. */
    private static final Map<String, String> ETIQUETAS = Map.of(
            "socket", "Socket",
            "ramTypeSupported", "Tipos de memoria admitidos",
            "ramType", "Tipo de memoria",
            "estimatedPowerW", "Consumo estimado (W)",
            "capacityW", "Potencia de la fuente (W)",
            "formFactor", "Formato");

    private final CalidadApiClient calidadApiClient;

    @GetMapping
    public String bandeja(@RequestParam(defaultValue = "false") boolean sinFuente, Model model,
            @AuthenticationPrincipal PcforgeUserDetails usuario) {
        model.addAttribute("pendientes",
                calidadApiClient.pending(sinFuente, usuario.getAuthorizationHeader()));
        model.addAttribute("cobertura", calidadApiClient.coverage(usuario.getAuthorizationHeader()));
        model.addAttribute("sinFuente", sinFuente);
        return "calidad";
    }

    @GetMapping("/{id}")
    public String ficha(@PathVariable String id, Model model,
            @AuthenticationPrincipal PcforgeUserDetails usuario) {
        CalidadApiClient.TechnicalSheet ficha = calidadApiClient.sheet(id, usuario.getAuthorizationHeader());
        model.addAttribute("ficha", ficha);
        model.addAttribute("faltantes", traducir(ficha.missingAttributes()));
        model.addAttribute("sinFuente", traducir(ficha.attributesWithoutSource()));
        model.addAttribute("requeridos", traducir(ficha.requiredAttributes()));
        return "calidad-ficha";
    }

    @PostMapping("/{id}")
    public String guardar(@PathVariable String id,
            @RequestParam(required = false) String socket,
            @RequestParam(required = false) String ramTypeSupported,
            @RequestParam(required = false) String ramType,
            @RequestParam(required = false) String estimatedPowerW,
            @RequestParam(required = false) String capacityW,
            @RequestParam(required = false) String formFactor,
            @RequestParam(required = false) String nota,
            @RequestParam(required = false) String fuente,
            @RequestParam(defaultValue = "false") boolean declararCompleta,
            @AuthenticationPrincipal PcforgeUserDetails usuario,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> specs = new LinkedHashMap<>();
        specs.put("socket", vacio(socket));
        specs.put("ramTypeSupported", lista(ramTypeSupported));
        specs.put("ramType", vacio(ramType));
        specs.put("estimatedPowerW", entero(estimatedPowerW, "El consumo estimado"));
        specs.put("capacityW", entero(capacityW, "La potencia de la fuente"));
        specs.put("formFactor", vacio(formFactor));
        if (nota != null && !nota.isBlank()) {
            specs.put("extra", Map.of("Nota", nota.trim()));
        }

        // La fuente se registra para cada atributo con valor: es lo que permite
        // explicar de donde salio el dato que sostiene una validacion.
        Map<String, String> sources = new LinkedHashMap<>();
        if (fuente != null && !fuente.isBlank()) {
            for (String clave : List.of("socket", "ramTypeSupported", "ramType", "estimatedPowerW", "capacityW",
                    "formFactor")) {
                Object valor = specs.get(clave);
                boolean conValor = valor instanceof List<?> listaValores ? !listaValores.isEmpty() : valor != null;
                if (conValor) {
                    sources.put(clave, fuente.trim());
                }
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("specs", specs);
        body.put("sources", sources);
        body.put("declareComplete", declararCompleta);

        calidadApiClient.updateAttributes(id, body, usuario.getAuthorizationHeader());
        redirectAttributes.addFlashAttribute("mensajeExito", declararCompleta
                ? "Ficha tecnica declarada completa, con la fuente registrada."
                : "Atributos tecnicos guardados. La ficha sigue abierta hasta que la declares completa.");
        return "redirect:/calidad/" + id;
    }

    private static String vacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static List<String> lista(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        List<String> valores = new ArrayList<>();
        for (String parte : csv.split(",")) {
            if (!parte.isBlank()) {
                valores.add(parte.trim());
            }
        }
        return valores;
    }

    private static Integer entero(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessMessageException(campo + " debe ser un numero entero.");
        }
    }

    private static List<Map<String, String>> traducir(List<String> claves) {
        if (claves == null) {
            return List.of();
        }
        return claves.stream()
                .map(clave -> Map.of("clave", clave, "etiqueta", ETIQUETAS.getOrDefault(clave, clave)))
                .toList();
    }

    public CalidadController(final CalidadApiClient calidadApiClient) {
        this.calidadApiClient = calidadApiClient;
    }
}
