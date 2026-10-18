package com.pcforge.catalogo.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;

/**
 * Datos de demostracion para los dos roles nuevos.
 *
 * El catalogo semilla no declara costo ni politica de margen porque esos datos
 * pertenecen a otros dos perfiles. Este cargador los completa una sola vez y de
 * forma idempotente: si un producto ya tiene costo, no lo toca. Asi la
 * demostracion funciona igual sobre una base nueva que sobre una que ya tenia
 * los productos cargados.
 *
 * Los factores estan elegidos para que existan casos de los tres estados:
 * referencias conformes, referencias bajo el margen minimo y una referencia con
 * margen casi nulo que justifica la alerta.
 */
@Configuration
@ConditionalOnProperty(name = "pcforge.seed.enabled", havingValue = "true")
public class PricingSeeder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PricingSeeder.class);

    /** Costo como fraccion del precio: 0.82 deja un margen bruto del 18 %. */
    private static final BigDecimal DEFAULT_COST_FACTOR = new BigDecimal("0.82");

    /** Excepciones deliberadas para provocar alertas de margen en la demostracion. */
    private static final Map<String, BigDecimal> COST_FACTORS = Map.of(
            "GPU-RTX4090-24G", new BigDecimal("0.94"),
            "CPU-I9-14900K", new BigDecimal("0.95"),
            "SSD-990PRO-1TB", new BigDecimal("0.93"),
            "RAM-DDR3-8-OEM", new BigDecimal("0.97"));

    /** Politica por categoria; el resto usa el valor global de la aplicacion. */
    private static final Map<String, BigDecimal> MARGIN_BY_SLUG = Map.of(
            "tarjetas-graficas", new BigDecimal("22.00"),
            "memorias-ram", new BigDecimal("15.00"),
            "accesorios", new BigDecimal("25.00"));

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Bean
    @Order(3)
    ApplicationRunner seedPricing() {
        return args -> {
            int costs = backfillCosts();
            int policies = backfillPolicies();
            if (costs > 0 || policies > 0) {
                log.info("Datos de precios completados: {} costos y {} politicas de margen", costs, policies);
            }
        };
    }

    private int backfillCosts() {
        List<Product> pending = productRepository.findAll().stream()
                .filter(p -> p.getCosto() == null && p.getPrice() != null)
                .toList();
        pending.forEach(product -> {
            BigDecimal factor = COST_FACTORS.getOrDefault(product.getSku(), DEFAULT_COST_FACTOR);
            product.setCosto(product.getPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP));
            productRepository.save(product);
        });
        return pending.size();
    }

    private int backfillPolicies() {
        List<Category> pending = categoryRepository.findAll().stream()
                .filter(c -> c.getMargenMinimo() == null)
                .toList();
        pending.forEach(category -> {
            category.setMargenMinimo(MARGIN_BY_SLUG.getOrDefault(category.getSlug(), new BigDecimal("18.00")));
            category.setUmbralDescuento(new BigDecimal("5.00"));
            category.setPolicyUpdatedBy("seed");
            category.setPolicyUpdatedAt(Instant.now());
            categoryRepository.save(category);
        });
        return pending.size();
    }

    public PricingSeeder(final ProductRepository productRepository, final CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
}
