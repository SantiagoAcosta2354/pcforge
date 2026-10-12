package com.pcforge.pedidos.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro de una solicitud a partir de una configuracion ya armada.
 * Los componentes y precios no se envian aqui: se toman del catalogo y de la
 * configuracion, nunca de lo que diga el navegador.
 */
public record CreateOrderRequest(

        @NotBlank(message = "La configuracion es obligatoria")
        String configurationId,

        @Size(max = 400, message = "La observacion no puede superar 400 caracteres")
        String customerNote) {
}
