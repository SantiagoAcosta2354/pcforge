package com.pcforge.compatibilidad.domain;

/**
 * Reglas de compatibilidad que el servicio evalua. Son pocas y explicitas
 * a proposito: no se pretende reproducir las listas de compatibilidad de
 * cada fabricante ni certificar el armado.
 */
public enum RuleCode {

    /** Coincidencia de socket entre procesador y placa base. */
    SOCKET,

    /** Tipo de memoria admitido por la placa base. */
    RAM_TYPE,

    /** Potencia estimada del conjunto frente a la capacidad de la fuente. */
    POWER
}
