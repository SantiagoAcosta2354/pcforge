package com.pcforge.catalogo.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Atributos obligatorios por tipo de componente y evaluacion de completitud
 * de la ficha tecnica.
 *
 * La regla es deliberadamente corta y explicita: cada tipo exige solo los
 * atributos que participan en las reglas de compatibilidad. Un accesorio no
 * exige ninguno y por eso su ficha siempre esta completa; un procesador exige
 * socket y consumo, porque sin ellos el validador no puede concluir nada.
 */
public final class TechnicalRequirements {

    private static final Map<ComponentType, List<String>> REQUIRED = new LinkedHashMap<>();

    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        REQUIRED.put(ComponentType.PROCESADOR, List.of("socket", "estimatedPowerW"));
        REQUIRED.put(ComponentType.PLACA_BASE, List.of("socket", "ramTypeSupported"));
        REQUIRED.put(ComponentType.MEMORIA_RAM, List.of("ramType"));
        REQUIRED.put(ComponentType.FUENTE_PODER, List.of("capacityW"));
        REQUIRED.put(ComponentType.TARJETA_GRAFICA, List.of("estimatedPowerW"));
        REQUIRED.put(ComponentType.GABINETE, List.of("formFactor"));

        LABELS.put("socket", "socket");
        LABELS.put("ramTypeSupported", "tipos de memoria admitidos");
        LABELS.put("ramType", "tipo de memoria");
        LABELS.put("estimatedPowerW", "consumo estimado en vatios");
        LABELS.put("capacityW", "capacidad de la fuente en vatios");
        LABELS.put("formFactor", "factor de forma");
    }

    private TechnicalRequirements() {
    }

    /** Atributos que el tipo de componente debe declarar para poder validarse. */
    public static List<String> required(ComponentType componentType) {
        if (componentType == null) {
            return List.of();
        }
        return REQUIRED.getOrDefault(componentType, List.of());
    }

    /** Atributos obligatorios que el producto todavia no declara. */
    public static List<String> missing(Product product) {
        List<String> missing = new ArrayList<>();
        if (product == null) {
            return missing;
        }
        TechnicalSpecs specs = product.getSpecs();
        for (String attribute : required(product.getComponentType())) {
            if (!hasValue(specs, attribute)) {
                missing.add(attribute);
            }
        }
        return missing;
    }

    /** Atributos obligatorios que el producto ya declara pero sin fuente registrada. */
    public static List<String> missingSource(Product product) {
        List<String> withoutSource = new ArrayList<>();
        if (product == null) {
            return withoutSource;
        }
        Map<String, AttributeSource> sources = product.getAttributeSources() == null
                ? Map.of()
                : product.getAttributeSources();
        for (String attribute : required(product.getComponentType())) {
            if (!hasValue(product.getSpecs(), attribute)) {
                continue;
            }
            AttributeSource source = sources.get(attribute);
            if (source == null || source.getSource() == null || source.getSource().isBlank()) {
                withoutSource.add(attribute);
            }
        }
        return withoutSource;
    }

    /** Nombre legible de un atributo, para los mensajes de error. */
    public static String label(String attribute) {
        return LABELS.getOrDefault(attribute, attribute);
    }

    /** Nombres legibles de una lista de atributos. */
    public static List<String> labels(List<String> attributes) {
        return attributes.stream().map(TechnicalRequirements::label).toList();
    }

    public static FichaStatus status(Product product) {
        return missing(product).isEmpty() ? FichaStatus.COMPLETA : FichaStatus.INCOMPLETA;
    }

    /** Un atributo obligatorio faltante deja la validacion de compatibilidad sin concluir. */
    public static boolean blocksCompatibility(Product product) {
        return !missing(product).isEmpty();
    }

    private static boolean hasValue(TechnicalSpecs specs, String attribute) {
        if (specs == null) {
            return false;
        }
        return switch (attribute) {
            case "socket" -> notBlank(specs.getSocket());
            case "ramType" -> notBlank(specs.getRamType());
            case "ramTypeSupported" -> specs.getRamTypeSupported() != null && !specs.getRamTypeSupported().isEmpty();
            case "estimatedPowerW" -> specs.getEstimatedPowerW() != null && specs.getEstimatedPowerW() > 0;
            case "capacityW" -> specs.getCapacityW() != null && specs.getCapacityW() > 0;
            case "formFactor" -> notBlank(specs.getFormFactor());
            default -> false;
        };
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
