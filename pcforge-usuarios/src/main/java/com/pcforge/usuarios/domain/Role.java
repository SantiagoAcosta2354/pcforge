package com.pcforge.usuarios.domain;

/**
 * Perfiles del sistema. Los mismos cuatro nombres viajan en el claim "roles"
 * del token y son los que autorizan las operaciones en los demas servicios.
 *
 * <ul>
 *   <li>ADMIN: administra cuentas, roles, catalogo e inventario.</li>
 *   <li>ASESOR: atiende solicitudes, revisa disponibilidad y registra cotizaciones.</li>
 *   <li>TECNICO: gestiona las ordenes de ensamblaje y sus etapas.</li>
 *   <li>CLIENTE: consulta el catalogo, configura equipos y hace pedidos.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    ASESOR,
    TECNICO,
    CLIENTE
}
