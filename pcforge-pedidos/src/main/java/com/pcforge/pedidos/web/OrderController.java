package com.pcforge.pedidos.web;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.pcforge.pedidos.config.CallerResolver;
import com.pcforge.pedidos.domain.OrderStatus;
import com.pcforge.pedidos.service.OrderService;
import com.pcforge.pedidos.web.dto.AddObservationRequest;
import com.pcforge.pedidos.web.dto.ChangeStatusRequest;
import com.pcforge.pedidos.web.dto.CreateOrderRequest;
import com.pcforge.pedidos.web.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos", description = "Cotizaciones, pedidos y etapas del ensamblaje")
public class OrderController {
    private final OrderService orderService;
    private final CallerResolver callerResolver;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una solicitud a partir de una configuracion armada")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, @AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return orderService.create(callerResolver.from(jwt, authorization), request);
    }

    @GetMapping
    @Operation(summary = "Lista los pedidos propios del cliente; el personal ve todos")
    public List<OrderResponse> list(@RequestParam(required = false) OrderStatus status, @AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return orderService.list(callerResolver.from(jwt, authorization), status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un pedido por su id o por su numero")
    public OrderResponse find(@PathVariable String id, @AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return orderService.find(id, callerResolver.from(jwt, authorization));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambia el estado del pedido segun las transiciones permitidas")
    public OrderResponse changeStatus(@PathVariable String id, @Valid @RequestBody ChangeStatusRequest request, @AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return orderService.changeStatus(id, request, callerResolver.from(jwt, authorization));
    }

    @PostMapping("/{id}/observations")
    @Operation(summary = "Agrega una observacion de seguimiento")
    public OrderResponse addObservation(@PathVariable String id, @Valid @RequestBody AddObservationRequest request, @AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return orderService.addObservation(id, request, callerResolver.from(jwt, authorization));
    }

    public OrderController(final OrderService orderService, final CallerResolver callerResolver) {
        this.orderService = orderService;
        this.callerResolver = callerResolver;
    }
}
