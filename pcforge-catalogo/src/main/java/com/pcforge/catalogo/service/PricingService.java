package com.pcforge.catalogo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.exception.ResourceNotFoundException;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.web.dto.CostRequest;
import com.pcforge.catalogo.web.dto.MarginResponse;
import com.pcforge.catalogo.web.dto.PricingPolicyRequest;
import com.pcforge.catalogo.web.dto.PricingPolicyResponse;

/**
 * Politica comercial de precios: margen minimo por categoria, alerta de las
 * referencias que quedan por debajo y registro del costo con el que se calcula.
 *
 * El margen se calcula sobre el precio de venta, que es la convencion con la
 * que se negocia en la tienda: un margen del 18 % significa que 18 de cada 100
 * pesos facturados son utilidad bruta, no que el precio sea un 18 % mayor que
 * el costo. La distincion importa porque cambia el numero y conviene decirla.
 */
@Service
public class PricingService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BigDecimal defaultMinimumMargin;
    private final BigDecimal defaultDiscountThreshold;

    public PricingService(final ProductRepository productRepository,
            final CategoryRepository categoryRepository,
            @Value("${pcforge.pricing.default-minimum-margin:18}") BigDecimal defaultMinimumMargin,
            @Value("${pcforge.pricing.default-discount-threshold:5}") BigDecimal defaultDiscountThreshold) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.defaultMinimumMargin = defaultMinimumMargin;
        this.defaultDiscountThreshold = defaultDiscountThreshold;
    }

    // ---------------------------------------------------------------- politica

    public List<PricingPolicyResponse> policies(boolean includeInactive) {
        List<Category> categories = includeInactive
                ? categoryRepository.findAll()
                : categoryRepository.findByActiveTrueOrderByNameAsc();
        return categories.stream()
                .map(c -> PricingPolicyResponse.from(c, defaultMinimumMargin, defaultDiscountThreshold))
                .toList();
    }

    @Transactional
    public PricingPolicyResponse updatePolicy(String categoryId, PricingPolicyRequest request, String actorId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoria " + categoryId));

        BigDecimal minimumMargin = scale(request.minimumMargin());
        if (minimumMargin.compareTo(BigDecimal.ZERO) < 0 || minimumMargin.compareTo(new BigDecimal("90")) > 0) {
            throw new BusinessRuleException("El margen minimo debe estar entre 0 y 90 por ciento");
        }
        BigDecimal threshold = request.discountThreshold() == null
                ? category.getUmbralDescuento()
                : scale(request.discountThreshold());
        if (threshold != null && (threshold.compareTo(BigDecimal.ZERO) < 0
                || threshold.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessRuleException("El umbral de descuento debe estar entre 0 y 100 por ciento");
        }

        category.setMargenMinimo(minimumMargin);
        category.setUmbralDescuento(threshold);
        category.setPolicyUpdatedBy(actorId);
        category.setPolicyUpdatedAt(Instant.now());

        return PricingPolicyResponse.from(categoryRepository.save(category),
                defaultMinimumMargin, defaultDiscountThreshold);
    }

    // ---------------------------------------------------------------- margenes

    /**
     * Referencias con su margen. Si onlyAlerts es verdadero se devuelven solo
     * las que estan por debajo del minimo o a las que les falta el costo, que
     * son las dos situaciones que el analista tiene que atender.
     */
    public List<MarginResponse> margins(String categoryId, boolean onlyAlerts, boolean includeInactive) {
        Map<String, Category> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a, LinkedHashMap::new));

        return productRepository.findAll().stream()
                .filter(p -> includeInactive || p.isActive())
                .filter(p -> categoryId == null || categoryId.isBlank() || categoryId.equals(p.getCategoryId()))
                .map(p -> toMargin(p, categories.get(p.getCategoryId())))
                .filter(m -> !onlyAlerts || !MarginResponse.STATUS_OK.equals(m.status()))
                .sorted(Comparator.comparing(MarginResponse::gap,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public MarginResponse margin(String productId) {
        Product product = requireProduct(productId);
        Category category = product.getCategoryId() == null ? null
                : categoryRepository.findById(product.getCategoryId()).orElse(null);
        return toMargin(product, category);
    }

    /**
     * El costo lo registra el administrador. El analista fija la politica, pero
     * no captura el dato que alimenta su propio control: esa separacion es la
     * que hace util la alerta de margen.
     */
    @Transactional
    public MarginResponse updateCost(String productId, CostRequest request, String actorId) {
        Product product = requireProduct(productId);
        if (request.cost() == null || request.cost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El costo del producto " + product.getSku() + " debe ser mayor que cero");
        }
        product.setCosto(scale(request.cost()));
        Product saved = productRepository.save(product);
        Category category = saved.getCategoryId() == null ? null
                : categoryRepository.findById(saved.getCategoryId()).orElse(null);
        return toMargin(saved, category);
    }

    // ---------------------------------------------------------------- apoyo

    private MarginResponse toMargin(Product product, Category category) {
        BigDecimal price = product.getPrice();
        BigDecimal cost = product.getCosto();
        BigDecimal minimum = category != null && category.getMargenMinimo() != null
                ? category.getMargenMinimo()
                : defaultMinimumMargin;
        String policySource = category != null && category.getMargenMinimo() != null ? "categoria" : "global";

        if (cost == null || price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return new MarginResponse(product.getId(), product.getSku(), product.getName(),
                    product.getCategoryId(), category == null ? null : category.getName(), product.isActive(),
                    cost, price, null, minimum, null, MarginResponse.STATUS_NO_COST, policySource);
        }

        BigDecimal margin = price.subtract(cost)
                .multiply(new BigDecimal("100"))
                .divide(price, 2, RoundingMode.HALF_UP);
        BigDecimal gap = minimum.subtract(margin).setScale(2, RoundingMode.HALF_UP);
        String status = margin.compareTo(minimum) < 0 ? MarginResponse.STATUS_BELOW : MarginResponse.STATUS_OK;

        return new MarginResponse(product.getId(), product.getSku(), product.getName(),
                product.getCategoryId(), category == null ? null : category.getName(), product.isActive(),
                cost, price, margin, minimum, gap, status, policySource);
    }

    private Product requireProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un producto con el id " + productId));
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
