package com.pcforge.compatibilidad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
import com.pcforge.compatibilidad.repository.CompatibilityRuleRepository;
import com.pcforge.compatibilidad.repository.ConfigurationRepository;
import com.pcforge.compatibilidad.repository.ValidationResultRepository;
import com.pcforge.compatibilidad.web.dto.ValidationResponse;

/**
 * Pruebas del motor de reglas. Se trabaja con repositorios y con el cliente
 * del catalogo simulados, de modo que las pruebas no dependen de una base de
 * datos ni de que el otro microservicio este en ejecucion.
 */
class CompatibilityServiceTest {

    private ConfigurationRepository configurationRepository;
    private ValidationResultRepository validationResultRepository;
    private CompatibilityRuleRepository ruleRepository;
    private CatalogoClient catalogoClient;
    private CompatibilityService service;

    private final Caller owner = new Caller("u-1", "cliente@pcforge.co", false);
    private final Caller intruder = new Caller("u-2", "otro@pcforge.co", false);
    private final Caller staff = new Caller("u-3", "asesor@pcforge.co", true);

    @BeforeEach
    void setUp() {
        configurationRepository = Mockito.mock(ConfigurationRepository.class);
        validationResultRepository = Mockito.mock(ValidationResultRepository.class);
        ruleRepository = Mockito.mock(CompatibilityRuleRepository.class);
        catalogoClient = Mockito.mock(CatalogoClient.class);
        service = new CompatibilityService(configurationRepository, validationResultRepository,
                ruleRepository, catalogoClient);

        when(ruleRepository.findByActiveTrue()).thenReturn(List.of(
                CompatibilityRule.builder().code(RuleCode.SOCKET).active(true).build(),
                CompatibilityRule.builder().code(RuleCode.RAM_TYPE).active(true).build(),
                CompatibilityRule.builder().code(RuleCode.POWER).active(true)
                        .parameters(Map.of("marginPercent", "20")).build()));
        when(validationResultRepository.save(any(ValidationResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(configurationRepository.save(any(Configuration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ------------------------------------------------------------ socket

    @Test
    @DisplayName("Socket compatible cuando el procesador y la placa comparten socket")
    void shouldReportCompatibleSocket() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105),
                board("AM5", List.of("DDR5")),
                memory("DDR5", 10),
                psu(650));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.OK);
        assertThat(response.missingData()).isEmpty();
        assertThat(response.checks()).anyMatch(c -> c.rule() == RuleCode.SOCKET
                && c.status() == CheckStatus.OK);
    }

    @Test
    @DisplayName("Socket incompatible cuando los sockets no coinciden")
    void shouldReportIncompatibleSocket() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105),
                board("LGA1700", List.of("DDR4")),
                memory("DDR4", 10),
                psu(650));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.INCOMPATIBLE);
        assertThat(response.checks()).anyMatch(c -> c.rule() == RuleCode.SOCKET
                && c.status() == CheckStatus.INCOMPATIBLE
                && c.message().contains("Socket incompatible"));
    }

    @Test
    @DisplayName("Socket no concluyente cuando el procesador no declara su socket")
    void shouldReportNotConclusiveWhenSocketIsMissing() {
        Configuration configuration = configurationWith(
                cpu(null, 105),
                board("AM5", List.of("DDR5")),
                memory("DDR5", 10),
                psu(650));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.NO_CONCLUYENTE);
        assertThat(response.missingData()).anyMatch(d -> d.contains("socket del procesador"));
    }

    // -------------------------------------------------------------- memoria

    @Test
    @DisplayName("Memoria incompatible cuando el tipo no esta entre los admitidos")
    void shouldReportIncompatibleMemory() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105),
                board("AM5", List.of("DDR5")),
                memory("DDR4", 10),
                psu(650));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.INCOMPATIBLE);
        assertThat(response.checks()).anyMatch(c -> c.rule() == RuleCode.RAM_TYPE
                && c.status() == CheckStatus.INCOMPATIBLE);
    }

    // -------------------------------------------------------------- potencia

    @Test
    @DisplayName("Potencia insuficiente cuando el consumo con margen supera la fuente")
    void shouldReportInsufficientPower() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105),
                board("AM5", List.of("DDR5")),
                memory("DDR5", 10),
                gpu(400),
                psu(450));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.INCOMPATIBLE);
        assertThat(response.checks()).anyMatch(c -> c.rule() == RuleCode.POWER
                && c.status() == CheckStatus.INCOMPATIBLE
                && c.message().contains("Potencia insuficiente"));
    }

    @Test
    @DisplayName("Potencia suficiente con una fuente que cubre el consumo y su margen")
    void shouldReportSufficientPower() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105),
                board("AM5", List.of("DDR5")),
                memory("DDR5", 10),
                gpu(400),
                psu(650));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.OK);
        assertThat(response.checks()).anyMatch(c -> c.rule() == RuleCode.POWER
                && c.status() == CheckStatus.OK
                && c.message().contains("Potencia suficiente"));
    }

    @Test
    @DisplayName("Potencia no concluyente cuando la tarjeta grafica no declara consumo")
    void shouldReportNotConclusiveWhenGraphicsPowerIsMissing() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105),
                board("AM5", List.of("DDR5")),
                memory("DDR5", 10),
                gpu(null),
                psu(650));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.NO_CONCLUYENTE);
        assertThat(response.missingData()).anyMatch(d -> d.contains("consumo estimado"));
    }

    @Test
    @DisplayName("Sin fuente de poder el resultado no es concluyente, no compatible")
    void shouldNotAssumeCompatibilityWithoutPowerSupply() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105),
                board("AM5", List.of("DDR5")),
                memory("DDR5", 10));

        ValidationResponse response = service.validate(given(configuration), owner);

        assertThat(response.result()).isEqualTo(CheckStatus.NO_CONCLUYENTE);
        assertThat(response.checks()).anyMatch(c -> c.rule() == RuleCode.POWER
                && c.status() == CheckStatus.NO_CONCLUYENTE);
    }

    // -------------------------------------------------------------- permisos

    @Test
    @DisplayName("Un cliente no puede validar la configuracion de otro usuario")
    void shouldDenyAccessToAnotherUsersConfiguration() {
        Configuration configuration = configurationWith(cpu("AM5", 105));
        when(configurationRepository.findById("c-1")).thenReturn(Optional.of(configuration));

        assertThatThrownBy(() -> service.validate("c-1", intruder))
                .isInstanceOf(AccessDeniedForUserException.class);
    }

    @Test
    @DisplayName("El personal autorizado si puede validar la configuracion de un cliente")
    void shouldAllowStaffToValidateAnyConfiguration() {
        Configuration configuration = configurationWith(
                cpu("AM5", 105), board("AM5", List.of("DDR5")), memory("DDR5", 10), psu(650));
        when(configurationRepository.findById("c-1")).thenReturn(Optional.of(configuration));

        ValidationResponse response = service.validate("c-1", staff);

        assertThat(response.result()).isEqualTo(CheckStatus.OK);
    }

    // ------------------------------------------------------------ catalogo

    @Test
    @DisplayName("No se incorpora un producto inactivo aunque exista en el catalogo")
    void shouldRejectInactiveProduct() {
        Configuration configuration = configurationWith();
        when(configurationRepository.findById("c-1")).thenReturn(Optional.of(configuration));
        when(catalogoClient.findProduct(anyString())).thenReturn(new CatalogoProduct(
                "p-9", "SKU-9", "Producto retirado", "PROCESADOR", new BigDecimal("100"),
                5, false, false, null));

        assertThatThrownBy(() -> service.addItem("c-1", "p-9", 1, owner))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactivo");
    }

    // ---------------------------------------------------------------- apoyo

    private String given(Configuration configuration) {
        when(configurationRepository.findById("c-1")).thenReturn(Optional.of(configuration));
        return "c-1";
    }

    private Configuration configurationWith(ConfigurationItem... items) {
        return Configuration.builder()
                .id("c-1")
                .ownerId("u-1")
                .ownerEmail("cliente@pcforge.co")
                .name("Equipo de prueba")
                .items(new java.util.ArrayList<>(List.of(items)))
                .build();
    }

    private ConfigurationItem cpu(String socket, Integer power) {
        return item("CPU-1", CompatibilityService.PROCESADOR,
                ConfigurationItem.ItemSpecs.builder().socket(socket).estimatedPowerW(power).build());
    }

    private ConfigurationItem board(String socket, List<String> supported) {
        return item("MB-1", CompatibilityService.PLACA_BASE,
                ConfigurationItem.ItemSpecs.builder().socket(socket).ramTypeSupported(supported).build());
    }

    private ConfigurationItem memory(String type, Integer power) {
        return item("RAM-1", CompatibilityService.MEMORIA_RAM,
                ConfigurationItem.ItemSpecs.builder().ramType(type).estimatedPowerW(power).build());
    }

    private ConfigurationItem gpu(Integer power) {
        return item("GPU-1", CompatibilityService.TARJETA_GRAFICA,
                ConfigurationItem.ItemSpecs.builder().estimatedPowerW(power).build());
    }

    private ConfigurationItem psu(Integer capacity) {
        return item("PSU-1", CompatibilityService.FUENTE_PODER,
                ConfigurationItem.ItemSpecs.builder().capacityW(capacity).build());
    }

    private ConfigurationItem item(String sku, String type, ConfigurationItem.ItemSpecs specs) {
        return ConfigurationItem.builder()
                .itemId(UUID.randomUUID().toString())
                .productId("id-" + sku)
                .sku(sku)
                .name(sku)
                .componentType(type)
                .price(new BigDecimal("100000"))
                .quantity(1)
                .specs(specs)
                .build();
    }
}
