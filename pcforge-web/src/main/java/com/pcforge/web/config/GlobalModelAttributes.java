package com.pcforge.web.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.pcforge.web.security.PcforgeUserDetails;

/**
 * Datos comunes a todas las pantallas: el usuario en sesion y las etiquetas
 * legibles de los codigos que usan los servicios. Centralizarlos evita repetir
 * el mismo condicional en cada plantilla.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("usuario")
    public PcforgeUserDetails usuario(@AuthenticationPrincipal PcforgeUserDetails usuario) {
        return usuario;
    }

    /** Nombre legible de los tipos de componente. */
    @ModelAttribute("etiquetasComponente")
    public Map<String, String> etiquetasComponente() {
        Map<String, String> etiquetas = new LinkedHashMap<>();
        etiquetas.put("PROCESADOR", "Procesador");
        etiquetas.put("PLACA_BASE", "Placa base");
        etiquetas.put("MEMORIA_RAM", "Memoria RAM");
        etiquetas.put("FUENTE_PODER", "Fuente de poder");
        etiquetas.put("TARJETA_GRAFICA", "Tarjeta grafica");
        etiquetas.put("ALMACENAMIENTO", "Almacenamiento");
        etiquetas.put("GABINETE", "Gabinete");
        etiquetas.put("REFRIGERACION", "Refrigeracion");
        etiquetas.put("OTRO", "Otro");
        return etiquetas;
    }

    /** Nombre legible de los estados de un pedido. */
    @ModelAttribute("etiquetasEstado")
    public Map<String, String> etiquetasEstado() {
        Map<String, String> etiquetas = new LinkedHashMap<>();
        etiquetas.put("RECIBIDO", "Recibido");
        etiquetas.put("EN_REVISION", "En revision");
        etiquetas.put("COTIZADO", "Cotizado");
        etiquetas.put("EN_ENSAMBLAJE", "En ensamblaje");
        etiquetas.put("EN_PRUEBAS", "En pruebas");
        etiquetas.put("LISTO_PARA_ENTREGA", "Listo para entrega");
        etiquetas.put("ENTREGADO", "Entregado");
        etiquetas.put("CANCELADO", "Cancelado");
        return etiquetas;
    }

    /** Resultado global de una validacion de compatibilidad. */
    @ModelAttribute("etiquetasResultado")
    public Map<String, String> etiquetasResultado() {
        Map<String, String> etiquetas = new LinkedHashMap<>();
        etiquetas.put("OK", "Compatible");
        etiquetas.put("INCOMPATIBLE", "Incompatible");
        etiquetas.put("NO_CONCLUYENTE", "No concluyente");
        return etiquetas;
    }

    /** Color de Bootstrap segun el resultado, para que la pantalla sea clara de un vistazo. */
    @ModelAttribute("colorResultado")
    public Map<String, String> colorResultado() {
        Map<String, String> colores = new LinkedHashMap<>();
        colores.put("OK", "success");
        colores.put("INCOMPATIBLE", "danger");
        colores.put("NO_CONCLUYENTE", "warning");
        return colores;
    }

    /** Color de Bootstrap segun el estado del pedido. */
    @ModelAttribute("colorEstado")
    public Map<String, String> colorEstado() {
        Map<String, String> colores = new LinkedHashMap<>();
        colores.put("RECIBIDO", "secondary");
        colores.put("EN_REVISION", "info");
        colores.put("COTIZADO", "primary");
        colores.put("EN_ENSAMBLAJE", "warning");
        colores.put("EN_PRUEBAS", "warning");
        colores.put("LISTO_PARA_ENTREGA", "success");
        colores.put("ENTREGADO", "dark");
        colores.put("CANCELADO", "danger");
        return colores;
    }
}
