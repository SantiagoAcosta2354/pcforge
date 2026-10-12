# generar-seeder.ps1
# Genera CatalogSeeder.java a partir de la fuente unica de datos.
# Uso: powershell -File generar-seeder.ps1

. "$PSScriptRoot\datos-catalogo.ps1"
$items = Get-CatalogoPCForge

$categorias = [ordered]@{
    'procesadores'      = @('Procesadores',   'Unidades centrales de proceso para equipos de escritorio')
    'placas-base'       = @('Placas base',    'Tarjetas madre que integran los componentes del equipo')
    'memorias-ram'      = @('Memorias RAM',   'Modulos de memoria principal')
    'fuentes-de-poder'  = @('Fuentes de poder','Fuentes de alimentacion para el conjunto de componentes')
    'tarjetas-graficas' = @('Tarjetas graficas','Unidades de procesamiento grafico')
    'almacenamiento'    = @('Almacenamiento', 'Unidades de almacenamiento internas del equipo')
    'gabinetes'         = @('Gabinetes',      'Chasis donde se montan los componentes')
    'refrigeracion'     = @('Refrigeracion',  'Disipacion del calor del procesador')
    'accesorios'        = @('Accesorios',     'Complementos que no participan en las reglas de compatibilidad')
}
$variable = @{
    'procesadores'='procesadores'; 'placas-base'='placas'; 'memorias-ram'='memorias'
    'fuentes-de-poder'='fuentes'; 'tarjetas-graficas'='graficas'; 'almacenamiento'='almacenamiento'
    'gabinetes'='gabinetes'; 'refrigeracion'='refrigeracion'; 'accesorios'='accesorios'
}

$sb = New-Object System.Text.StringBuilder

# ---------------------------------------------------------------- categorias
foreach ($slug in $categorias.Keys) {
    $nombre = $categorias[$slug][0]
    $desc   = $categorias[$slug][1]
    $var    = $variable[$slug]
    [void]$sb.AppendLine("            Category $var = categoryRepository.save(Category.builder().name(`"$nombre`").slug(`"$slug`").description(`"$desc`").build());")
}

# ----------------------------------------------------------------- productos
foreach ($slug in $categorias.Keys) {
    $var = $variable[$slug]
    [void]$sb.AppendLine("            // --- $($categorias[$slug][0]) ---")
    foreach ($it in ($items | Where-Object { $_.cat -eq $slug })) {
        if ($it.viejo -eq $null -and $it.sku -eq 'CPU-RYZEN5-5600X') { }
        $linea = "            productRepository.save(Product.builder().sku(`"$($it.sku)`").name(`"$($it.name)`").brand(`"$($it.brand)`").categoryId($var.getId()).componentType(ComponentType.$($it.tipo)).description(`"$($it.desc)`").price(new BigDecimal(`"$($it.price).00`")).stock($($it.stock)).minimumStock($($it.min))"

        $specs = @()
        if ($it.socket)    { $specs += "socket(`"$($it.socket)`")" }
        if ($it.ramSup)    { $specs += "ramTypeSupported(List.of(" + (($it.ramSup | ForEach-Object { "`"$_`"" }) -join ', ') + "))" }
        if ($it.ramType)   { $specs += "ramType(`"$($it.ramType)`")" }
        if ($it.powerW)    { $specs += "estimatedPowerW($($it.powerW))" }
        if ($it.capW)      { $specs += "capacityW($($it.capW))" }
        if ($it.form)      { $specs += "formFactor(`"$($it.form)`")" }
        if ($it.extra) {
            $pares = @()
            foreach ($k in $it.extra.Keys) { $pares += "`"$k`", `"$($it.extra[$k])`"" }
            $specs += "extra(Map.of(" + ($pares -join ', ') + "))"
        }

        if ($specs.Count -gt 0) {
            $linea += ".specs(TechnicalSpecs.builder()." + ($specs -join '.') + ".build())"
        }
        $linea += ".build());"
        [void]$sb.AppendLine($linea)
    }
}

# ------------------------------------------------------------ archivo entero
$cabecera = @'
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
 * Catalogo de demostracion con componentes reales de las plataformas vigentes:
 * AM4, AM5, LGA1700 y LGA1851, con DDR4 y DDR5, fuentes desde 450 W y graficas
 * desde 115 W hasta 450 W.
 *
 * Los precios son ilustrativos (pesos colombianos) y no son cotizaciones de
 * mercado: sirven para que la tienda de demostracion tenga cifras coherentes.
 *
 * Tres productos declaran datos incompletos a proposito (un procesador sin
 * socket, una placa sin tipos de memoria y una grafica sin consumo) para poder
 * comprobar que el validador avisa del dato faltante en lugar de afirmar una
 * compatibilidad que no puede sostener.
 *
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
'@

$pie = @'
            log.info("Datos de demostracion cargados: {} productos en {} categorias", productRepository.count(), categoryRepository.count());
        };
    }

    public CatalogSeeder(final CategoryRepository categoryRepository, final ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }
}
'@

$contenido = $cabecera + "`r`n" + $sb.ToString().TrimEnd("`r","`n") + "`r`n" + $pie
$contenido = $contenido -replace "`r`n", "`n" -replace "`n", "`r`n"

$destino = Join-Path $PSScriptRoot 'pcforge-catalogo\src\main\java\com\pcforge\catalogo\seed\CatalogSeeder.java'
[System.IO.File]::WriteAllText($destino, $contenido, (New-Object System.Text.UTF8Encoding($false)))

Write-Host "CatalogSeeder.java generado: $($contenido.Length) caracteres"
Write-Host "  categorias : $($categorias.Count)"
Write-Host "  productos  : $($items.Count)"
Write-Host "  sin specs  : $(@($items | Where-Object { -not $_.socket -and -not $_.ramType -and -not $_.ramSup -and -not $_.powerW -and -not $_.capW -and -not $_.form }).Count)"
