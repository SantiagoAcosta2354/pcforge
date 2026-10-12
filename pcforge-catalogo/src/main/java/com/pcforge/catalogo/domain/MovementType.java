package com.pcforge.catalogo.domain;

/**
 * Tipo de movimiento de inventario. Permite auditar por que cambio la
 * existencia de un producto en lugar de sobrescribir el valor.
 */
public enum MovementType {
    ENTRADA,
    SALIDA,
    AJUSTE
}
