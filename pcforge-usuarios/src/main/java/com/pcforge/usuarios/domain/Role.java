package com.pcforge.usuarios.domain;

/**
 * Perfiles del sistema. Los mismos seis nombres viajan en el claim "roles"
 * del token y son los que autorizan las operaciones en los demas servicios.
 *
 * <ul>
 *   <li>ADMIN: administra cuentas, roles, catalogo, inventario y costo.</li>
 *   <li>ASESOR: atiende solicitudes, revisa disponibilidad y registra cotizaciones.</li>
 *   <li>TECNICO: gestiona las ordenes de ensamblaje y sus etapas.</li>
 *   <li>CLIENTE: consulta el catalogo, configura equipos y hace pedidos.</li>
 *   <li>CURADOR: gobierna el dato tecnico del catalogo, registra la fuente de
 *       cada atributo y declara la ficha completa. No toca precio ni existencia.</li>
 *   <li>ANALISTA: define la politica de margen y descuento por categoria y
 *       consulta las referencias que quedan por debajo del minimo.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    ASESOR,
    TECNICO,
    CLIENTE,
    CURADOR,
    ANALISTA
}
