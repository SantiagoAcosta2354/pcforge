package com.pcforge.catalogo.seed;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import com.pcforge.catalogo.domain.Category;
import com.pcforge.catalogo.domain.ComponentType;
import com.pcforge.catalogo.domain.Product;
import com.pcforge.catalogo.domain.TechnicalSpecs;
import com.pcforge.catalogo.repository.CategoryRepository;
import com.pcforge.catalogo.repository.ProductRepository;

/**
 * Datos de demostracion. Estan revisados a mano y son pocos a proposito:
 * el catalogo de prueba debe permitir provocar tanto resultados compatibles
 * como incompatibles y no concluyentes cuando falten atributos.
 * No se ejecuta si la base ya tiene informacion.
 */
@Configuration
@ConditionalOnProperty(name = "pcforge.seed.enabled", havingValue = "true")
public class CatalogSeeder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CatalogSeeder.class);
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // despues de crear los indices, para que las reglas unicas ya existan
    @Bean
    @Order(2)
    ApplicationRunner seedCatalog() {
        return args -> {
            if (categoryRepository.count() > 0) {
                log.info("El catalogo ya tiene datos; no se cargan los datos de demostracion");
                return;
            }
            Category procesadores = categoryRepository.save(Category.builder().name("Procesadores").slug("procesadores").description("Unidades centrales de proceso para equipos de escritorio").build());
            Category placas = categoryRepository.save(Category.builder().name("Placas base").slug("placas-base").description("Tarjetas madre que integran los componentes del equipo").build());
            Category memorias = categoryRepository.save(Category.builder().name("Memorias RAM").slug("memorias-ram").description("Modulos de memoria principal").build());
            Category fuentes = categoryRepository.save(Category.builder().name("Fuentes de poder").slug("fuentes-de-poder").description("Fuentes de alimentacion para el conjunto de componentes").build());
            Category graficas = categoryRepository.save(Category.builder().name("Tarjetas graficas").slug("tarjetas-graficas").description("Unidades de procesamiento grafico").build());
            Category almacenamiento = categoryRepository.save(Category.builder().name("Almacenamiento").slug("almacenamiento").description("Unidades de almacenamiento internas del equipo").build());
            Category gabinetes = categoryRepository.save(Category.builder().name("Gabinetes").slug("gabinetes").description("Chasis donde se montan los componentes").build());
            Category refrigeracion = categoryRepository.save(Category.builder().name("Refrigeracion").slug("refrigeracion").description("Disipacion del calor del procesador").build());
            Category accesorios = categoryRepository.save(Category.builder().name("Accesorios").slug("accesorios").description("Complementos que no participan en las reglas de compatibilidad").build());
            // --- Procesadores ---
            productRepository.save(Product.builder().sku("CPU-AM5-7600").name("Procesador 6 nucleos AM5").brand("AMD").categoryId(procesadores.getId()).componentType(ComponentType.PROCESADOR).description("Procesador de escritorio para socket AM5, orientado a equipos de juego y trabajo.").price(new BigDecimal("749000.00")).stock(8).minimumStock(2).specs(TechnicalSpecs.builder().socket("AM5").estimatedPowerW(105).extra(Map.of("nucleos", "6", "frecuencia", "3.8 GHz")).build()).build());
            productRepository.save(Product.builder().sku("CPU-LGA1700-12400").name("Procesador 6 nucleos LGA1700").brand("Intel").categoryId(procesadores.getId()).componentType(ComponentType.PROCESADOR).description("Procesador de escritorio para socket LGA1700.").price(new BigDecimal("689000.00")).stock(5).minimumStock(2).specs(TechnicalSpecs.builder().socket("LGA1700").estimatedPowerW(95).build()).build());
            // Falta el socket a proposito: permite demostrar el resultado no concluyente.
            productRepository.save(Product.builder().sku("CPU-OEM-IND").name("Procesador sin especificacion publicada").brand("Generica").categoryId(procesadores.getId()).componentType(ComponentType.PROCESADOR).description("Producto de prueba que no declara socket, para verificar el caso no concluyente.").price(new BigDecimal("399000.00")).stock(3).minimumStock(1).specs(TechnicalSpecs.builder().estimatedPowerW(65).build()).build());
            productRepository.save(Product.builder().sku("CPU-AM5-8600").name("Procesador 8 nucleos AM5 de alto rendimiento").brand("AMD").categoryId(procesadores.getId()).componentType(ComponentType.PROCESADOR).description("Procesador de escritorio para socket AM5 orientado a equipos exigentes.").price(new BigDecimal("1450000.00")).stock(6).minimumStock(2).specs(TechnicalSpecs.builder().socket("AM5").estimatedPowerW(120).extra(Map.of("nucleos", "8", "frecuencia", "4.2 GHz")).build()).build());
            productRepository.save(Product.builder().sku("CPU-LGA1700-1470").name("Procesador 20 nucleos LGA1700").brand("Intel").categoryId(procesadores.getId()).componentType(ComponentType.PROCESADOR).description("Procesador de escritorio de gama alta para socket LGA1700.").price(new BigDecimal("1890000.00")).stock(4).minimumStock(1).specs(TechnicalSpecs.builder().socket("LGA1700").estimatedPowerW(190).extra(Map.of("nucleos", "20", "frecuencia", "3.4 GHz")).build()).build());
            productRepository.save(Product.builder().sku("CPU-LGA1851-2450").name("Procesador 14 nucleos LGA1851").brand("Intel").categoryId(procesadores.getId()).componentType(ComponentType.PROCESADOR).description("Procesador de escritorio para el socket LGA1851.").price(new BigDecimal("1250000.00")).stock(3).minimumStock(1).specs(TechnicalSpecs.builder().socket("LGA1851").estimatedPowerW(125).extra(Map.of("nucleos", "14", "frecuencia", "3.9 GHz")).build()).build());
            productRepository.save(Product.builder().sku("CPU-AM4-5700").name("Procesador 8 nucleos AM4").brand("AMD").categoryId(procesadores.getId()).componentType(ComponentType.PROCESADOR).description("Procesador de escritorio para socket AM4, la opcion mas economica del catalogo.").price(new BigDecimal("620000.00")).stock(5).minimumStock(2).specs(TechnicalSpecs.builder().socket("AM4").estimatedPowerW(65).extra(Map.of("nucleos", "8", "frecuencia", "3.4 GHz")).build()).build());
            // --- Placas base ---
            productRepository.save(Product.builder().sku("MB-AM5-DDR5").name("Placa base AM5 con DDR5").brand("MSI").categoryId(placas.getId()).componentType(ComponentType.PLACA_BASE).description("Placa base para socket AM5 con soporte de memoria DDR5.").price(new BigDecimal("895000.00")).stock(6).minimumStock(2).specs(TechnicalSpecs.builder().socket("AM5").ramTypeSupported(List.of("DDR5")).formFactor("ATX").build()).build());
            productRepository.save(Product.builder().sku("MB-LGA1700-DDR4").name("Placa base LGA1700 con DDR4").brand("Gigabyte").categoryId(placas.getId()).componentType(ComponentType.PLACA_BASE).description("Placa base para socket LGA1700 con soporte de memoria DDR4.").price(new BigDecimal("720000.00")).stock(4).minimumStock(1).specs(TechnicalSpecs.builder().socket("LGA1700").ramTypeSupported(List.of("DDR4")).formFactor("microATX").build()).build());
            productRepository.save(Product.builder().sku("MB-AM5-ITX").name("Placa base AM5 compacta ITX").brand("ASRock").categoryId(placas.getId()).componentType(ComponentType.PLACA_BASE).description("Placa base AM5 en formato ITX, pensada para equipos compactos.").price(new BigDecimal("1090000.00")).stock(3).minimumStock(1).specs(TechnicalSpecs.builder().socket("AM5").ramTypeSupported(List.of("DDR5")).formFactor("ITX").build()).build());
            productRepository.save(Product.builder().sku("MB-LGA1851-DDR5").name("Placa base LGA1851 con DDR5").brand("Asus").categoryId(placas.getId()).componentType(ComponentType.PLACA_BASE).description("Placa base para socket LGA1851 con soporte de memoria DDR5.").price(new BigDecimal("1180000.00")).stock(4).minimumStock(1).specs(TechnicalSpecs.builder().socket("LGA1851").ramTypeSupported(List.of("DDR5")).formFactor("ATX").build()).build());
            productRepository.save(Product.builder().sku("MB-AM4-DDR4").name("Placa base AM4 con DDR4").brand("MSI").categoryId(placas.getId()).componentType(ComponentType.PLACA_BASE).description("Placa base para socket AM4 con soporte de memoria DDR4.").price(new BigDecimal("520000.00")).stock(5).minimumStock(2).specs(TechnicalSpecs.builder().socket("AM4").ramTypeSupported(List.of("DDR4")).formFactor("microATX").build()).build());
            // Falta el tipo de memoria a proposito: el socket si coincide, pero la regla de memoria queda no concluyente.
            productRepository.save(Product.builder().sku("MB-AM5-SIN-RAM").name("Placa base AM5 sin tipos de memoria publicados").brand("Generica").categoryId(placas.getId()).componentType(ComponentType.PLACA_BASE).description("Producto de prueba que declara el socket pero no los tipos de memoria que admite.").price(new BigDecimal("610000.00")).stock(2).minimumStock(1).specs(TechnicalSpecs.builder().socket("AM5").formFactor("ATX").build()).build());
            // --- Memorias ---
            productRepository.save(Product.builder().sku("RAM-DDR5-16").name("Memoria DDR5 16 GB").brand("Kingston").categoryId(memorias.getId()).componentType(ComponentType.MEMORIA_RAM).description("Modulo de memoria DDR5 de 16 GB.").price(new BigDecimal("310000.00")).stock(10).minimumStock(3).specs(TechnicalSpecs.builder().ramType("DDR5").estimatedPowerW(10).build()).build());
            productRepository.save(Product.builder().sku("RAM-DDR4-8").name("Memoria DDR4 8 GB").brand("Crucial").categoryId(memorias.getId()).componentType(ComponentType.MEMORIA_RAM).description("Modulo de memoria DDR4 de 8 GB.").price(new BigDecimal("165000.00")).stock(12).minimumStock(4).specs(TechnicalSpecs.builder().ramType("DDR4").estimatedPowerW(8).build()).build());
            productRepository.save(Product.builder().sku("RAM-DDR5-32").name("Memoria DDR5 32 GB").brand("Kingston").categoryId(memorias.getId()).componentType(ComponentType.MEMORIA_RAM).description("Modulo de memoria DDR5 de 32 GB.").price(new BigDecimal("585000.00")).stock(6).minimumStock(2).specs(TechnicalSpecs.builder().ramType("DDR5").estimatedPowerW(12).build()).build());
            productRepository.save(Product.builder().sku("RAM-DDR4-16").name("Memoria DDR4 16 GB").brand("Crucial").categoryId(memorias.getId()).componentType(ComponentType.MEMORIA_RAM).description("Modulo de memoria DDR4 de 16 GB.").price(new BigDecimal("295000.00")).stock(8).minimumStock(3).specs(TechnicalSpecs.builder().ramType("DDR4").estimatedPowerW(9).build()).build());
            productRepository.save(Product.builder().sku("RAM-DDR3-8").name("Memoria DDR3 8 GB").brand("Generica").categoryId(memorias.getId()).componentType(ComponentType.MEMORIA_RAM).description("Modulo de memoria DDR3 de 8 GB, incompatible con las placas vigentes del catalogo.").price(new BigDecimal("95000.00")).stock(6).minimumStock(2).specs(TechnicalSpecs.builder().ramType("DDR3").estimatedPowerW(7).build()).build());
            // --- Fuentes ---
            productRepository.save(Product.builder().sku("PSU-650W-80P").name("Fuente 650 W certificada").brand("EVGA").categoryId(fuentes.getId()).componentType(ComponentType.FUENTE_PODER).description("Fuente de alimentacion de 650 W.").price(new BigDecimal("420000.00")).stock(7).minimumStock(2).specs(TechnicalSpecs.builder().capacityW(650).build()).build());
            productRepository.save(Product.builder().sku("PSU-450W-BAS").name("Fuente 450 W basica").brand("Generica").categoryId(fuentes.getId()).componentType(ComponentType.FUENTE_PODER).description("Fuente de alimentacion de 450 W, insuficiente para equipos de alto consumo.").price(new BigDecimal("185000.00")).stock(9).minimumStock(3).specs(TechnicalSpecs.builder().capacityW(450).build()).build());
            productRepository.save(Product.builder().sku("PSU-850W-80G").name("Fuente 850 W certificada Gold").brand("Corsair").categoryId(fuentes.getId()).componentType(ComponentType.FUENTE_PODER).description("Fuente de alimentacion de 850 W para equipos de alto consumo.").price(new BigDecimal("690000.00")).stock(5).minimumStock(1).specs(TechnicalSpecs.builder().capacityW(850).build()).build());
            productRepository.save(Product.builder().sku("PSU-1000W-80P").name("Fuente 1000 W certificada Platinum").brand("Seasonic").categoryId(fuentes.getId()).componentType(ComponentType.FUENTE_PODER).description("Fuente de alimentacion de 1000 W para configuraciones exigentes.").price(new BigDecimal("1150000.00")).stock(3).minimumStock(1).specs(TechnicalSpecs.builder().capacityW(1000).build()).build());
            // --- Tarjetas graficas ---
            productRepository.save(Product.builder().sku("GPU-RTX-4060").name("Tarjeta grafica 8 GB").brand("ASUS").categoryId(graficas.getId()).componentType(ComponentType.TARJETA_GRAFICA).description("Tarjeta grafica de gama media para equipos de juego.").price(new BigDecimal("1580000.00")).stock(3).minimumStock(1).specs(TechnicalSpecs.builder().estimatedPowerW(180).build()).build());
            productRepository.save(Product.builder().sku("GPU-RTX-4070").name("Tarjeta grafica 12 GB").brand("Gigabyte").categoryId(graficas.getId()).componentType(ComponentType.TARJETA_GRAFICA).description("Tarjeta grafica de gama alta para equipos de juego y edicion.").price(new BigDecimal("2450000.00")).stock(3).minimumStock(1).specs(TechnicalSpecs.builder().estimatedPowerW(200).build()).build());
            productRepository.save(Product.builder().sku("GPU-RTX-4090").name("Tarjeta grafica 24 GB de alto consumo").brand("ASUS").categoryId(graficas.getId()).componentType(ComponentType.TARJETA_GRAFICA).description("Tarjeta grafica de gama entusiasta; exige una fuente de alta capacidad.").price(new BigDecimal("6900000.00")).stock(2).minimumStock(1).specs(TechnicalSpecs.builder().estimatedPowerW(320).build()).build());
            // Falta el consumo a proposito: permite provocar el resultado no concluyente en la regla de potencia.
            productRepository.save(Product.builder().sku("GPU-OEM-IND").name("Tarjeta grafica sin consumo publicado").brand("Generica").categoryId(graficas.getId()).componentType(ComponentType.TARJETA_GRAFICA).description("Producto de prueba que no declara su consumo, para verificar el caso no concluyente de potencia.").price(new BigDecimal("890000.00")).stock(2).minimumStock(1).specs(TechnicalSpecs.builder().extra(Map.of("memoria", "8 GB")).build()).build());
            // --- Almacenamiento ---
            productRepository.save(Product.builder().sku("SSD-NVME-1TB").name("Unidad SSD NVMe 1 TB").brand("Samsung").categoryId(almacenamiento.getId()).componentType(ComponentType.ALMACENAMIENTO).description("Unidad de estado solido NVMe PCIe 4.0 de 1 TB.").price(new BigDecimal("420000.00")).stock(10).minimumStock(3).specs(TechnicalSpecs.builder().estimatedPowerW(8).extra(Map.of("interfaz", "NVMe PCIe 4.0", "capacidad", "1 TB")).build()).build());
            productRepository.save(Product.builder().sku("SSD-SATA-480").name("Unidad SSD SATA 480 GB").brand("Kingston").categoryId(almacenamiento.getId()).componentType(ComponentType.ALMACENAMIENTO).description("Unidad de estado solido SATA III de 480 GB.").price(new BigDecimal("195000.00")).stock(12).minimumStock(4).specs(TechnicalSpecs.builder().estimatedPowerW(5).extra(Map.of("interfaz", "SATA III", "capacidad", "480 GB")).build()).build());
            productRepository.save(Product.builder().sku("HDD-SATA-2TB").name("Disco duro mecanico 2 TB").brand("Seagate").categoryId(almacenamiento.getId()).componentType(ComponentType.ALMACENAMIENTO).description("Disco duro de 2 TB a 7200 rpm para almacenamiento masivo.").price(new BigDecimal("310000.00")).stock(8).minimumStock(2).specs(TechnicalSpecs.builder().estimatedPowerW(9).extra(Map.of("interfaz", "SATA III", "capacidad", "2 TB", "rpm", "7200")).build()).build());
            // --- Gabinetes ---
            productRepository.save(Product.builder().sku("CASE-ATX-MID").name("Gabinete de torre media ATX").brand("NZXT").categoryId(gabinetes.getId()).componentType(ComponentType.GABINETE).description("Gabinete de torre media compatible con placas ATX y microATX.").price(new BigDecimal("380000.00")).stock(6).minimumStock(2).specs(TechnicalSpecs.builder().formFactor("ATX").extra(Map.of("bahias", "4", "ventiladores", "2")).build()).build());
            productRepository.save(Product.builder().sku("CASE-ITX-SLIM").name("Gabinete compacto ITX").brand("Cooler Master").categoryId(gabinetes.getId()).componentType(ComponentType.GABINETE).description("Gabinete compacto para placas ITX, con espacio reducido para refrigeracion.").price(new BigDecimal("260000.00")).stock(5).minimumStock(2).specs(TechnicalSpecs.builder().formFactor("ITX").extra(Map.of("bahias", "2", "ventiladores", "1")).build()).build());
            // --- Refrigeracion ---
            productRepository.save(Product.builder().sku("COOL-AIR-150").name("Disipador de aire para 150 W").brand("Cooler Master").categoryId(refrigeracion.getId()).componentType(ComponentType.REFRIGERACION).description("Disipador por aire con capacidad declarada de 150 W de TDP.").price(new BigDecimal("210000.00")).stock(8).minimumStock(3).specs(TechnicalSpecs.builder().estimatedPowerW(4).extra(Map.of("tipo", "aire", "tdp", "150 W")).build()).build());
            productRepository.save(Product.builder().sku("COOL-LIQ-240").name("Refrigeracion liquida 240 mm").brand("Corsair").categoryId(refrigeracion.getId()).componentType(ComponentType.REFRIGERACION).description("Kit de refrigeracion liquida con radiador de 240 mm.").price(new BigDecimal("690000.00")).stock(4).minimumStock(1).specs(TechnicalSpecs.builder().estimatedPowerW(12).extra(Map.of("tipo", "liquida", "tdp", "250 W", "radiador", "240 mm")).build()).build());
            // --- Accesorios ---
            // Sin atributos tecnicos a proposito: comprueba que el configurador acepta un componente sin especificaciones.
            productRepository.save(Product.builder().sku("ACC-CABLES-KIT").name("Kit de cables modulares").brand("Generica").categoryId(accesorios.getId()).componentType(ComponentType.OTRO).description("Juego de cables de alimentacion modular; no participa en ninguna regla de compatibilidad.").price(new BigDecimal("85000.00")).stock(15).minimumStock(5).build());
            log.info("Datos de demostracion cargados: {} productos en {} categorias", productRepository.count(), categoryRepository.count());
        };
    }

    public CatalogSeeder(final CategoryRepository categoryRepository, final ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }
}
