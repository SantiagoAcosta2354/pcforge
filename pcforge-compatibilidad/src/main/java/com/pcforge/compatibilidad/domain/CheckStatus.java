package com.pcforge.compatibilidad.domain;

/**
 * Resultado de una comprobacion.
 * NO_CONCLUYENTE es un resultado legitimo: significa que faltan datos tecnicos
 * y el sistema prefiere decirlo antes que afirmar una compatibilidad.
 */
public enum CheckStatus {
    OK,
    INCOMPATIBLE,
    NO_CONCLUYENTE
}
