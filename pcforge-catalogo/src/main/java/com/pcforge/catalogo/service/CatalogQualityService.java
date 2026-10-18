package com.pcforge.catalogo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcforge.catalogo.domain.AttributeSource;
import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.TechnicalRequirements;
import com.pcforge.catalogo.exception.BusinessRuleException;
import com.pcforge.catalogo.exception.ResourceNotFoundException;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;
import com.pcforge.catalogo.web.dto.CoverageResponse;
import com.pcforge.catalogo.web.dto.PendingSheetResponse;
import com.pcforge.catalogo.web.dto.TechnicalDataRequest;
import com.pcforge.catalogo.web.dto.TechnicalDataResponse;

/**
 * Gobierno del dato tecnico del catalogo: es la responsabilidad del curador.
 *
 * La idea que sostiene este servicio es simple y conviene explicarla: la
 * validacion de compatibilidad solo puede ser confiable si los atributos que
 * consume existen y tienen fuente. Por eso aqui no se "completa la ficha" como
 * quien llena un formulario, sino que se registra de donde salio cada dato y
 * se declara la ficha verificada cuando ya no falta ningun obligatorio.
 */
@Service
public class CatalogQualityService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CatalogQualityService(final ProductRepository productRepository,
            final CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ---------------------------------------------------------------- consultas

    /**
     * Bandeja de trabajo. Primero lo que bloquea una regla de compatibilidad,
     * porque una ficha incompleta que nadie consulta no molesta, pero la que
     * impide validar socket o consumo deja al configurador sin respuesta.
     */
    public List<PendingSheetResponse> pending(boolean includeInactive, boolean includeUnverified) {
        Map<String, String> categoryNames = categoryNames();
        return productRepository.findAll().stream()
                .filter(p -> includeInactive || p.isActive())
                .filter(p -> !TechnicalRequirements.missing(p).isEmpty()
                        || (includeUnverified && !TechnicalRequirements.missingSource(p).isEmpty()))
                .sorted(Comparator
                        .comparing((Product p) -> TechnicalRequirements.missing(p).isEmpty())
                        .thenComparing(Product::getSku))
                .map(p -> PendingSheetResponse.from(p, categoryNames.get(p.getCategoryId())))
                .toList();
    }

    public TechnicalDataResponse find(String productId) {
        Product product = requireProduct(productId);
        return TechnicalDataResponse.from(product, categoryName(product.getCategoryId()));
    }

    public CoverageResponse coverage() {
        Map<String, String> categoryNames = categoryNames();
        List<Product> products = productRepository.findAll();

        List<Product> incomplete = products.stream()
                .filter(p -> !TechnicalRequirements.missing(p).isEmpty())
                .toList();

        Map<String, List<Product>> byCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategoryId, LinkedHashMap::new, Collectors.toList()));

        List<CoverageResponse.CategoryCoverage> categories = new ArrayList<>();
        byCategory.forEach((categoryId, items) -> {
            int complete = (int) items.stream().filter(p -> TechnicalRequirements.missing(p).isEmpty()).count();
            categories.add(new CoverageResponse.CategoryCoverage(
                    categoryId, categoryNames.get(categoryId), items.size(), complete,
                    items.size() - complete, percentage(complete, items.size())));
        });
        categories.sort(Comparator.comparing(CoverageResponse.CategoryCoverage::categoryName,
                Comparator.nullsLast(Comparator.naturalOrder())));

        Map<String, Integer> missingCount = new LinkedHashMap<>();
        incomplete.forEach(p -> TechnicalRequirements.missing(p)
                .forEach(attribute -> missingCount.merge(attribute, 1, Integer::sum)));

        List<CoverageResponse.MissingAttribute> topMissing = missingCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> new CoverageResponse.MissingAttribute(e.getKey(),
                        TechnicalRequirements.label(e.getKey()), e.getValue()))
                .toList();

        int complete = products.size() - incomplete.size();
        return new CoverageResponse(products.size(), complete, incomplete.size(), incomplete.size(),
                percentage(complete, products.size()), categories, topMissing);
    }

    // ---------------------------------------------------------------- comandos

    /**
     * Registra atributos tecnicos y, opcionalmente, declara la ficha completa.
     *
     * Dos reglas se aplican aqui y no en la interfaz:
     * 1. No se declara completa una ficha a la que le falta un obligatorio.
     * 2. No se declara completa una ficha cuyos obligatorios no tienen fuente.
     * La segunda es la que evita que el catalogo "parezca" confiable sin serlo.
     */
    @Transactional
    public TechnicalDataResponse updateAttributes(String productId, TechnicalDataRequest request, String actorId) {
        Product product = requireProduct(productId);

        if (request.specs() != null) {
            product.setSpecs(request.specs());
        }
        registerSources(product, request.sources(), actorId);

        List<String> missing = TechnicalRequirements.missing(product);
        boolean declareComplete = Boolean.TRUE.equals(request.declareComplete());

        if (declareComplete) {
            if (!missing.isEmpty()) {
                throw new BusinessRuleException("No se puede declarar completa la ficha del producto "
                        + product.getSku() + ": faltan " + String.join(", ", TechnicalRequirements.labels(missing)));
            }
            List<String> withoutSource = TechnicalRequirements.missingSource(product);
            if (!withoutSource.isEmpty()) {
                throw new BusinessRuleException("No se puede declarar completa la ficha del producto "
                        + product.getSku() + ": registra la fuente de "
                        + String.join(", ", TechnicalRequirements.labels(withoutSource)));
            }
            product.setVerifiedAt(Instant.now());
            product.setVerifiedBy(actorId);
        } else if (!missing.isEmpty()) {
            // Si se retiro un obligatorio, la verificacion anterior deja de ser valida.
            product.setVerifiedAt(null);
            product.setVerifiedBy(null);
        }

        return TechnicalDataResponse.from(productRepository.save(product),
                categoryName(product.getCategoryId()));
    }

    private void registerSources(Product product, Map<String, String> sources, String actorId) {
        if (sources == null || sources.isEmpty()) {
            return;
        }
        Map<String, AttributeSource> current = product.getAttributeSources() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(product.getAttributeSources());
        Instant now = Instant.now();
        sources.forEach((attribute, source) -> {
            if (source == null || source.isBlank()) {
                throw new BusinessRuleException("La fuente del atributo " + TechnicalRequirements.label(attribute)
                        + " no puede estar vacia");
            }
            current.put(attribute, new AttributeSource(source.trim(), now, actorId));
        });
        product.setAttributeSources(current);
    }

    // ---------------------------------------------------------------- apoyo

    private Map<String, String> categoryNames() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a, LinkedHashMap::new));
    }

    private String categoryName(String categoryId) {
        return categoryRepository.findById(categoryId).map(Category::getName).orElse(null);
    }

    private Product requireProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un producto con el id " + productId));
    }

    private double percentage(int part, int total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((part * 10000.0) / total) / 100.0;
    }
}
