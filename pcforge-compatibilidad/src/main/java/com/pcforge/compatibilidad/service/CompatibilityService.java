package com.pcforge.compatibilidad.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pcforge.compatibilidad.client.CatalogoClient;
import com.pcforge.compatibilidad.client.CatalogoProduct;
import com.pcforge.compatibilidad.domain.CheckStatus;
import com.pcforge.compatibilidad.domain.CompatibilityRule;
import com.pcforge.compatibilidad.domain.Configuration;
import com.pcforge.compatibilidad.domain.ConfigurationItem;
import com.pcforge.compatibilidad.domain.RuleCode;
import com.pcforge.compatibilidad.domain.ValidationResult;
import com.pcforge.compatibilidad.exception.AccessDeniedForUserException;
import com.pcforge.compatibilidad.exception.BusinessRuleException;
import com.pcforge.compatibilidad.exception.ResourceNotFoundException;
import com.pcforge.compatibilidad.repository.CompatibilityRuleRepository;
import com.pcforge.compatibilidad.repository.ConfigurationRepository;
import com.pcforge.compatibilidad.repository.ValidationResultRepository;
import com.pcforge.compatibilidad.web.dto.ConfigurationResponse;
import com.pcforge.compatibilidad.web.dto.ValidationResponse;

/**
 * Configurador de equipos y motor de validacion.
 *
 * Las reglas son deliberadamente pocas y explicitas: coincidencia de socket,
 * tipo de memoria admitido por la placa y potencia estimada frente a la fuente.
 * El servicio no pretende certificar un armado. Ante datos incompletos el
 * resultado es NO_CONCLUYENTE y se indica que atributo falta, porque afirmar
 * una compatibilidad que no se puede sostener es peor que admitir la duda.
 */
@Service
public class CompatibilityService {
    static final String PROCESADOR = "PROCESADOR";
    static final String PLACA_BASE = "PLACA_BASE";
    static final String MEMORIA_RAM = "MEMORIA_RAM";
    static final String FUENTE_PODER = "FUENTE_PODER";
    static final String TARJETA_GRAFICA = "TARJETA_GRAFICA";
    private final ConfigurationRepository configurationRepository;
    private final ValidationResultRepository validationResultRepository;
    private final CompatibilityRuleRepository ruleRepository;
    private final CatalogoClient catalogoClient;

    // -------------------------------------------------------- configuraciones
    @Transactional
    public ConfigurationResponse create(Caller caller, String name) {
        Configuration configuration = Configuration.builder().ownerId(caller.userId()).ownerEmail(caller.email()).name(name == null || name.isBlank() ? "Configuracion sin nombre" : name.trim()).build();
        return ConfigurationResponse.from(configurationRepository.save(configuration));
    }

    public List<ConfigurationResponse> list(Caller caller) {
        List<Configuration> configurations = caller.staff() ? configurationRepository.findAll() : configurationRepository.findByOwnerIdOrderByUpdatedAtDesc(caller.userId());
        return configurations.stream().map(ConfigurationResponse::from).toList();
    }

    public ConfigurationResponse get(String id, Caller caller) {
        return ConfigurationResponse.from(requireAccessible(id, caller));
    }

    @Transactional
    public void delete(String id, Caller caller) {
        configurationRepository.delete(requireAccessible(id, caller));
    }

    @Transactional
    public ConfigurationResponse addItem(String id, String productId, Integer quantity, Caller caller) {
        Configuration configuration = requireAccessible(id, caller);
        CatalogoProduct product = catalogoClient.findProduct(productId);
        if (!product.active()) {
            throw new BusinessRuleException("El producto " + product.sku() + " esta inactivo y no se puede incorporar a una configuracion");
        }
        int amount = quantity == null ? 1 : quantity;
        // Si el componente ya estaba, se suma la cantidad en lugar de duplicarlo.
        for (ConfigurationItem existing : configuration.getItems()) {
            if (existing.getProductId().equals(productId)) {
                existing.setQuantity(existing.getQuantity() + amount);
                return ConfigurationResponse.from(configurationRepository.save(configuration));
            }
        }
        configuration.getItems().add(ConfigurationItem.builder().itemId(UUID.randomUUID().toString()).productId(product.id()).sku(product.sku()).name(product.name()).componentType(product.componentType()).price(product.price()).quantity(amount).specs(toItemSpecs(product)).build());
        return ConfigurationResponse.from(configurationRepository.save(configuration));
    }

    @Transactional
    public ConfigurationResponse removeItem(String id, String itemId, Caller caller) {
        Configuration configuration = requireAccessible(id, caller);
        boolean removed = configuration.getItems().removeIf(item -> item.getItemId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("La configuracion no contiene el componente " + itemId);
        }
        return ConfigurationResponse.from(configurationRepository.save(configuration));
    }

    // ------------------------------------------------------------- validacion
    @Transactional
    public ValidationResponse validate(String id, Caller caller) {
        Configuration configuration = requireAccessible(id, caller);
        Map<RuleCode, CompatibilityRule> activeRules = ruleRepository.findByActiveTrue().stream().collect(Collectors.toMap(CompatibilityRule::getCode, Function.identity(), (a, b) -> a));
        List<ValidationResult.RuleCheck> checks = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        if (activeRules.containsKey(RuleCode.SOCKET)) {
            checks.add(checkSocket(configuration, missing));
        }
        if (activeRules.containsKey(RuleCode.RAM_TYPE)) {
            checks.add(checkRam(configuration, missing));
        }
        if (activeRules.containsKey(RuleCode.POWER)) {
            checks.add(checkPower(configuration, activeRules.get(RuleCode.POWER), missing));
        }
        CheckStatus global = aggregate(checks);
        ValidationResult saved = validationResultRepository.save(ValidationResult.builder().configurationId(configuration.getId()).requestedBy(caller.userId()).result(global).checks(checks).missingData(missing).createdAt(Instant.now()).build());
        configuration.setLastResult(global.name());
        configurationRepository.save(configuration);
        return ValidationResponse.from(saved);
    }

    public List<ValidationResponse> history(String id, Caller caller) {
        Configuration configuration = requireAccessible(id, caller);
        return validationResultRepository.findByConfigurationIdOrderByCreatedAtDesc(configuration.getId()).stream().map(ValidationResponse::from).toList();
    }

    // ------------------------------------------------------- motor de reglas
    private ValidationResult.RuleCheck checkSocket(Configuration configuration, List<String> missing) {
        List<ConfigurationItem> processors = itemsOfType(configuration, PROCESADOR);
        List<ConfigurationItem> boards = itemsOfType(configuration, PLACA_BASE);
        if (processors.isEmpty()) {
            return check(RuleCode.SOCKET, CheckStatus.NO_CONCLUYENTE, "No se puede evaluar el socket: la configuracion aun no incluye un procesador");
        }
        if (boards.isEmpty()) {
            return check(RuleCode.SOCKET, CheckStatus.NO_CONCLUYENTE, "No se puede evaluar el socket: la configuracion aun no incluye una placa base");
        }
        List<String> processorSockets = socketsOf(processors, "socket del procesador", missing);
        List<String> boardSockets = socketsOf(boards, "socket de la placa base", missing);
        if (processorSockets.isEmpty() || boardSockets.isEmpty()) {
            return check(RuleCode.SOCKET, CheckStatus.NO_CONCLUYENTE, "No se puede evaluar el socket porque falta el dato en alguno de los componentes");
        }
        boolean matches = processorSockets.stream().anyMatch(boardSockets::contains);
        if (!matches) {
            return check(RuleCode.SOCKET, CheckStatus.INCOMPATIBLE, "Socket incompatible: el procesador usa " + String.join(", ", processorSockets) + " y la placa base admite " + String.join(", ", boardSockets));
        }
        return check(RuleCode.SOCKET, CheckStatus.OK, "Socket compatible: " + String.join(", ", processorSockets));
    }

    private ValidationResult.RuleCheck checkRam(Configuration configuration, List<String> missing) {
        List<ConfigurationItem> boards = itemsOfType(configuration, PLACA_BASE);
        List<ConfigurationItem> memories = itemsOfType(configuration, MEMORIA_RAM);
        if (boards.isEmpty()) {
            return check(RuleCode.RAM_TYPE, CheckStatus.NO_CONCLUYENTE, "No se puede evaluar la memoria: la configuracion aun no incluye una placa base");
        }
        if (memories.isEmpty()) {
            return check(RuleCode.RAM_TYPE, CheckStatus.NO_CONCLUYENTE, "No se puede evaluar la memoria: la configuracion aun no incluye modulos de memoria");
        }
        Set<String> supported = boards.stream().map(ConfigurationItem::getSpecs).filter(Objects::nonNull).map(ConfigurationItem.ItemSpecs::getRamTypeSupported).filter(Objects::nonNull).flatMap(List::stream).map(String::toUpperCase).collect(Collectors.toSet());
        if (supported.isEmpty()) {
            missing.add("tipos de memoria admitidos por la placa base (" + skusOf(boards) + ")");
            return check(RuleCode.RAM_TYPE, CheckStatus.NO_CONCLUYENTE, "No se puede evaluar la memoria porque la placa base no declara que tipos admite");
        }
        for (ConfigurationItem memory : memories) {
            String type = memory.getSpecs() == null ? null : memory.getSpecs().getRamType();
            if (type == null || type.isBlank()) {
                missing.add("tipo de memoria del modulo " + memory.getSku());
                return check(RuleCode.RAM_TYPE, CheckStatus.NO_CONCLUYENTE, "No se puede evaluar el modulo " + memory.getSku() + " porque no declara su tipo de memoria");
            }
            if (!supported.contains(type.toUpperCase())) {
                return check(RuleCode.RAM_TYPE, CheckStatus.INCOMPATIBLE, "Memoria incompatible: el modulo " + memory.getSku() + " es " + type + " y la placa base admite " + String.join(", ", supported));
            }
        }
        return check(RuleCode.RAM_TYPE, CheckStatus.OK, "Memoria compatible: la placa base admite " + String.join(", ", supported));
    }

    private ValidationResult.RuleCheck checkPower(Configuration configuration, CompatibilityRule rule, List<String> missing) {
        List<ConfigurationItem> powerSupplies = itemsOfType(configuration, FUENTE_PODER);
        if (powerSupplies.isEmpty()) {
            return check(RuleCode.POWER, CheckStatus.NO_CONCLUYENTE, "No se puede estimar la potencia: la configuracion aun no incluye una fuente de poder");
        }
        List<Integer> capacities = powerSupplies.stream().map(ConfigurationItem::getSpecs).filter(Objects::nonNull).map(ConfigurationItem.ItemSpecs::getCapacityW).filter(Objects::nonNull).toList();
        if (capacities.isEmpty()) {
            missing.add("capacidad en vatios de la fuente (" + skusOf(powerSupplies) + ")");
            return check(RuleCode.POWER, CheckStatus.NO_CONCLUYENTE, "No se puede estimar la potencia porque la fuente no declara su capacidad");
        }
        int capacity = capacities.stream().mapToInt(Integer::intValue).min().orElseThrow();
        // Los componentes que consumen de verdad deben declarar su consumo.
        List<ConfigurationItem> significant = configuration.getItems().stream().filter(i -> PROCESADOR.equals(i.getComponentType()) || TARJETA_GRAFICA.equals(i.getComponentType())).toList();
        for (ConfigurationItem item : significant) {
            Integer power = item.getSpecs() == null ? null : item.getSpecs().getEstimatedPowerW();
            if (power == null) {
                missing.add("consumo estimado en vatios de " + item.getSku());
                return check(RuleCode.POWER, CheckStatus.NO_CONCLUYENTE, "No se puede estimar la potencia total porque " + item.getSku() + " no declara su consumo");
            }
        }
        int declared = configuration.getItems().stream().map(ConfigurationItem::getSpecs).filter(Objects::nonNull).map(ConfigurationItem.ItemSpecs::getEstimatedPowerW).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        int marginPercent = readMarginPercent(rule);
        double required = declared * (1 + marginPercent / 100.0);
        if (required > capacity) {
            return check(RuleCode.POWER, CheckStatus.INCOMPATIBLE, String.format("Potencia insuficiente: el conjunto consume %d W y con el margen del %d%% " + "se requieren %.0f W, pero la fuente declara %d W", declared, marginPercent, required, capacity));
        }
        return check(RuleCode.POWER, CheckStatus.OK, String.format("Potencia suficiente: %d W declarados, %.0f W requeridos con el margen del %d%%, " + "frente a %d W de la fuente", declared, required, marginPercent, capacity));
    }

    private int readMarginPercent(CompatibilityRule rule) {
        if (rule.getParameters() == null) {
            return 20;
        }
        return rule.getParameters().entrySet().stream().filter(e -> "marginPercent".equalsIgnoreCase(e.getKey())).map(Map.Entry::getValue).map(value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return 20;
            }
        }).findFirst().orElse(20);
    }

    /**
     * Una incompatibilidad pesa mas que una duda; la duda pesa mas que un OK.
     */
    private CheckStatus aggregate(List<ValidationResult.RuleCheck> checks) {
        if (checks.isEmpty()) {
            return CheckStatus.NO_CONCLUYENTE;
        }
        if (checks.stream().anyMatch(c -> c.getStatus() == CheckStatus.INCOMPATIBLE)) {
            return CheckStatus.INCOMPATIBLE;
        }
        if (checks.stream().anyMatch(c -> c.getStatus() == CheckStatus.NO_CONCLUYENTE)) {
            return CheckStatus.NO_CONCLUYENTE;
        }
        return CheckStatus.OK;
    }

    private ValidationResult.RuleCheck check(RuleCode rule, CheckStatus status, String message) {
        return ValidationResult.RuleCheck.builder().rule(rule).status(status).message(message).build();
    }

    // ----------------------------------------------------------------- apoyo
    private List<ConfigurationItem> itemsOfType(Configuration configuration, String componentType) {
        return configuration.getItems().stream().filter(item -> componentType.equals(item.getComponentType())).toList();
    }

    private List<String> socketsOf(List<ConfigurationItem> items, String label, List<String> missing) {
        List<String> sockets = new ArrayList<>();
        for (ConfigurationItem item : items) {
            String socket = item.getSpecs() == null ? null : item.getSpecs().getSocket();
            if (socket == null || socket.isBlank()) {
                missing.add(label + " (" + item.getSku() + ")");
            } else if (!sockets.contains(socket)) {
                sockets.add(socket);
            }
        }
        return sockets;
    }

    private String skusOf(List<ConfigurationItem> items) {
        return items.stream().map(ConfigurationItem::getSku).collect(Collectors.joining(", "));
    }

    private ConfigurationItem.ItemSpecs toItemSpecs(CatalogoProduct product) {
        CatalogoProduct.Specs specs = product.specs();
        if (specs == null) {
            return null;
        }
        return ConfigurationItem.ItemSpecs.builder().socket(specs.socket()).ramTypeSupported(specs.ramTypeSupported()).ramType(specs.ramType()).estimatedPowerW(specs.estimatedPowerW()).capacityW(specs.capacityW()).formFactor(specs.formFactor()).build();
    }

    /**
     * Una configuracion solo es accesible para su dueÃ±o o para el personal.
     */
    private Configuration requireAccessible(String id, Caller caller) {
        Configuration configuration = configurationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe la configuracion " + id));
        if (!caller.staff() && !Objects.equals(configuration.getOwnerId(), caller.userId())) {
            throw new AccessDeniedForUserException("La configuracion pertenece a otro usuario");
        }
        return configuration;
    }

    public CompatibilityService(final ConfigurationRepository configurationRepository, final ValidationResultRepository validationResultRepository, final CompatibilityRuleRepository ruleRepository, final CatalogoClient catalogoClient) {
        this.configurationRepository = configurationRepository;
        this.validationResultRepository = validationResultRepository;
        this.ruleRepository = ruleRepository;
        this.catalogoClient = catalogoClient;
    }
}
