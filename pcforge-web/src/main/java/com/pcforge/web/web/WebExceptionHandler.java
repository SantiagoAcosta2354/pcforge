package com.pcforge.web.web;

import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.pcforge.web.exception.BusinessMessageException;
import com.pcforge.web.exception.DownstreamUnavailableException;
import com.pcforge.web.exception.NotFoundInViewException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Traduce las excepciones de la capa web a pantallas comprensibles.
 *
 * Las reglas de negocio no rompen la navegacion: el mensaje se devuelve a la
 * pagina desde la que se hizo la accion mediante un atributo flash, de modo
 * que el usuario no pierda el contexto ni tenga que volver atras a mano.
 */
@ControllerAdvice
public class WebExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(BusinessMessageException.class)
    public String handleBusiness(BusinessMessageException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        return "redirect:" + safeReturnPath(request);
    }

    @ExceptionHandler(NotFoundInViewException.class)
    public String handleNotFound(NotFoundInViewException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error/no-encontrado";
    }

    @ExceptionHandler(DownstreamUnavailableException.class)
    public String handleDownstream(DownstreamUnavailableException ex, Model model) {
        log.warn("Servicio no disponible: {}", ex.getMessage());
        model.addAttribute("mensaje", ex.getMessage());
        return "error/servicio-no-disponible";
    }

    /**
     * Devuelve solo la ruta de la pagina de origen. Se toma del encabezado
     * Referer, pero se descarta todo lo que no sea una ruta interna para no
     * habilitar una redireccion abierta hacia otro sitio.
     */
    private String safeReturnPath(HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        try {
            URI uri = URI.create(referer);
            if (uri.getHost() != null && !uri.getHost().equals(request.getServerName())) {
                return "/";
            }
            String path = uri.getRawPath();
            if (path == null || !path.startsWith("/") || path.startsWith("//")) {
                return "/";
            }
            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException ex) {
            return "/";
        }
    }
}
