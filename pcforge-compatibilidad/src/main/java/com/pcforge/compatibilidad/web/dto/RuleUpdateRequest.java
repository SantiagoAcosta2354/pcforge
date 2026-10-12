package com.pcforge.compatibilidad.web.dto;

import java.util.Map;

/**
 * Ajuste de una regla. Permite cambiar el margen de potencia o desactivar una
 * regla sin recompilar el servicio.
 */
public record RuleUpdateRequest(Boolean active, Map<String, String> parameters) {
}
