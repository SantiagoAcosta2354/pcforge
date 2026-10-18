package com.pcforge.catalogo.domain;

/**
 * Estado de completitud de la ficha tecnica de un producto.
 * No es un campo almacenado: se deduce de los atributos obligatorios que el
 * tipo de componente exige, de modo que nunca queda desincronizado con los
 * datos reales del producto.
 */
public enum FichaStatus {
    COMPLETA,
    INCOMPLETA
}
