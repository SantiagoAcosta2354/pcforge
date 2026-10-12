package com.pcforge.catalogo.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.service.ProductService;
import com.pcforge.catalogo.service.StockService;
import com.pcforge.catalogo.web.dto.ProductRequest;
import com.pcforge.catalogo.web.dto.ProductResponse;
import com.pcforge.catalogo.web.dto.StockAdjustmentRequest;
import com.pcforge.catalogo.web.dto.StockMovementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Catalogo", description = "Productos, atributos tecnicos y existencias")
public class ProductController {
    private final ProductService productService;
    private final StockService stockService;

    @GetMapping
    @Operation(summary = "Lista o busca productos del catalogo")
    public List<ProductResponse> list(@RequestParam(required = false) String categoryId, @RequestParam(required = false) ComponentType componentType, @RequestParam(required = false) String name, @RequestParam(defaultValue = "false") boolean includeInactive) {
        return productService.search(categoryId, componentType, name, includeInactive);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Productos activos que alcanzaron su existencia minima")
    public List<ProductResponse> lowStock() {
        return productService.lowStock();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un producto por su identificador")
    public ProductResponse findById(@PathVariable String id) {
        return productService.findById(id);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Consulta un producto por su SKU")
    public ProductResponse findBySku(@PathVariable String sku) {
        return productService.findBySku(sku);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un producto nuevo")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto existente")
    public ProductResponse update(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activa o desactiva un producto sin eliminarlo")
    public ProductResponse changeStatus(@PathVariable String id, @RequestParam boolean active) {
        return productService.changeStatus(id, active);
    }

    @PostMapping("/{id}/stock")
    @Operation(summary = "Registra una entrada, una salida o un ajuste de existencia")
    public ProductResponse adjustStock(@PathVariable String id, @Valid @RequestBody StockAdjustmentRequest request, @AuthenticationPrincipal Jwt jwt) {
        String actorId = jwt != null ? jwt.getSubject() : null;
        return stockService.adjust(id, request, actorId);
    }

    @GetMapping("/{id}/stock/movements")
    @Operation(summary = "Historial de movimientos de existencia de un producto")
    public List<StockMovementResponse> movements(@PathVariable String id) {
        return stockService.history(id);
    }

    public ProductController(final ProductService productService, final StockService stockService) {
        this.productService = productService;
        this.stockService = stockService;
    }
}
