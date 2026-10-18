package com.pcforge.catalogo.web.dto;

import java.util.Map;

import com.pcforge.catalogo.domain.TechnicalSpecs;

/**
 * Datos que envia el curador de datos tecnicos. No incluye precio ni
 * existencia: esos campos no son de su competencia y el endpoint que atiende
 * esta solicitud no puede modificarlos.
 *
 * sources: clave del atributo (socket, ramType, capacityW...) contra la
 * referencia de la fuente que respalda el dato.
 */
public record TechnicalDataRequest(
        TechnicalSpecs specs,
        Map<String, String> sources,
        Boolean declareComplete) {
}
