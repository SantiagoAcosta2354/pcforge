package com.pcforge.catalogo.domain;

/**
 * Tipos de componente que la tienda comercializa.
 * El tipo determina que atributos tecnicos son relevantes para las reglas
 * de compatibilidad: un procesador tiene socket, una fuente tiene capacidad.
 */
public enum ComponentType {
    PROCESADOR,
    PLACA_BASE,
    MEMORIA_RAM,
    FUENTE_PODER,
    TARJETA_GRAFICA,
    ALMACENAMIENTO,
    GABINETE,
    REFRIGERACION,
    OTRO
}
