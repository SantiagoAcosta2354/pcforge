# cargar-catalogo.ps1
# Rellena el catalogo en ejecucion con los componentes nuevos del seed.
#
# Por que existe este script: CatalogSeeder solo carga datos cuando la base esta
# vacia (para no pisar informacion real). Como la base de Atlas ya tenia el
# catalogo antiguo, las categorias y productos añadidos al seeder no se cargan
# solos. Este script hace esa carga una sola vez, por API y con token de ADMIN.
#
# Es idempotente: omite lo que ya existe (compara por slug y por SKU).
#
# Uso: powershell -ExecutionPolicy Bypass -File C:\workspace-sts\cargar-catalogo.ps1

$ErrorActionPreference = 'Stop'

$UsuariosUrl = 'http://localhost:8081'
$CatalogoUrl = 'http://localhost:8082'
$AdminEmail  = 'admin@pcforge.co'
$AdminPass   = 'Admin1234*'

function Send-Json {
    param([string]$Method, [string]$Url, [string]$Token, $Body)
    $headers = @{}
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }
    $json = if ($null -eq $Body) { '' } else { $Body | ConvertTo-Json -Depth 8 -Compress }
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers -ContentType 'application/json; charset=utf-8' -Body $bytes
}

# --- 1. Autenticacion --------------------------------------------------------
Write-Host 'Autenticando como administrador...'
$login = Send-Json -Method Post -Url "$UsuariosUrl/api/v1/auth/login" -Body @{ email = $AdminEmail; password = $AdminPass }
$token = $login.token
if (-not $token) { $token = $login.accessToken }
if (-not $token) { throw "El servicio de usuarios no devolvio un token. Respuesta: $($login | ConvertTo-Json -Compress)" }
Write-Host '  Token obtenido.'

# --- 2. Categorias -----------------------------------------------------------
$nuevasCategorias = @(
    @{ name = 'Almacenamiento'; slug = 'almacenamiento'; description = 'Unidades de almacenamiento internas del equipo' }
    @{ name = 'Gabinetes';      slug = 'gabinetes';      description = 'Chasis donde se montan los componentes' }
    @{ name = 'Refrigeracion';  slug = 'refrigeracion';  description = 'Disipacion del calor del procesador' }
    @{ name = 'Accesorios';     slug = 'accesorios';     description = 'Complementos que no participan en las reglas de compatibilidad' }
)

Write-Host ''
Write-Host 'Categorias:'
$mapa = @{}
foreach ($cat in (Invoke-RestMethod -Uri "$CatalogoUrl/api/v1/categories?includeInactive=true" -TimeoutSec 20)) {
    $mapa[$cat.slug] = $cat.id
}
foreach ($cat in $nuevasCategorias) {
    if ($mapa.ContainsKey($cat.slug)) {
        Write-Host "  = ya existe: $($cat.slug)"
    } else {
        $creada = Send-Json -Method Post -Url "$CatalogoUrl/api/v1/categories" -Token $token -Body $cat
        $mapa[$cat.slug] = $creada.id
        Write-Host "  + creada:    $($cat.slug) -> $($creada.id)"
    }
}

# --- 3. Productos ------------------------------------------------------------
function Specs { param($Socket, $RamSupported, $RamType, $Power, $Capacity, $FormFactor, $Extra)
    $s = @{}
    if ($Socket)       { $s['socket'] = $Socket }
    if ($RamSupported) { $s['ramTypeSupported'] = @($RamSupported) }
    if ($RamType)      { $s['ramType'] = $RamType }
    if ($Power)        { $s['estimatedPowerW'] = $Power }
    if ($Capacity)     { $s['capacityW'] = $Capacity }
    if ($FormFactor)   { $s['formFactor'] = $FormFactor }
    if ($Extra)        { $s['extra'] = $Extra }
    if ($s.Count -eq 0) { return $null }
    return $s
}

$nuevos = @(
    # --- Procesadores ---
    @{ sku='CPU-AM5-8600';     name='Procesador 8 nucleos AM5 de alto rendimiento'; brand='AMD';           cat='procesadores';    type='PROCESADOR';      price=1450000; stock=6;  min=2; desc='Procesador de escritorio para socket AM5 orientado a equipos exigentes.';  specs=(Specs -Socket 'AM5' -Power 120 -Extra @{nucleos='8'; frecuencia='4.2 GHz'}) }
    @{ sku='CPU-LGA1700-1470'; name='Procesador 20 nucleos LGA1700';                brand='Intel';         cat='procesadores';    type='PROCESADOR';      price=1890000; stock=4;  min=1; desc='Procesador de escritorio de gama alta para socket LGA1700.';              specs=(Specs -Socket 'LGA1700' -Power 190 -Extra @{nucleos='20'; frecuencia='3.4 GHz'}) }
    @{ sku='CPU-LGA1851-2450'; name='Procesador 14 nucleos LGA1851';                brand='Intel';         cat='procesadores';    type='PROCESADOR';      price=1250000; stock=3;  min=1; desc='Procesador de escritorio para el socket LGA1851.';                       specs=(Specs -Socket 'LGA1851' -Power 125 -Extra @{nucleos='14'; frecuencia='3.9 GHz'}) }
    @{ sku='CPU-AM4-5700';     name='Procesador 8 nucleos AM4';                     brand='AMD';           cat='procesadores';    type='PROCESADOR';      price=620000;  stock=5;  min=2; desc='Procesador de escritorio para socket AM4, la opcion mas economica del catalogo.'; specs=(Specs -Socket 'AM4' -Power 65 -Extra @{nucleos='8'; frecuencia='3.4 GHz'}) }
    # --- Placas base ---
    @{ sku='MB-AM5-ITX';       name='Placa base AM5 compacta ITX';                  brand='ASRock';        cat='placas-base';     type='PLACA_BASE';      price=1090000; stock=3;  min=1; desc='Placa base AM5 en formato ITX, pensada para equipos compactos.';          specs=(Specs -Socket 'AM5' -RamSupported @('DDR5') -FormFactor 'ITX') }
    @{ sku='MB-LGA1851-DDR5';  name='Placa base LGA1851 con DDR5';                  brand='Asus';          cat='placas-base';     type='PLACA_BASE';      price=1180000; stock=4;  min=1; desc='Placa base para socket LGA1851 con soporte de memoria DDR5.';             specs=(Specs -Socket 'LGA1851' -RamSupported @('DDR5') -FormFactor 'ATX') }
    @{ sku='MB-AM4-DDR4';      name='Placa base AM4 con DDR4';                     brand='MSI';           cat='placas-base';     type='PLACA_BASE';      price=520000;  stock=5;  min=2; desc='Placa base para socket AM4 con soporte de memoria DDR4.';                 specs=(Specs -Socket 'AM4' -RamSupported @('DDR4') -FormFactor 'microATX') }
    @{ sku='MB-AM5-SIN-RAM';   name='Placa base AM5 sin tipos de memoria publicados'; brand='Generica';    cat='placas-base';     type='PLACA_BASE';      price=610000;  stock=2;  min=1; desc='Producto de prueba que declara el socket pero no los tipos de memoria que admite.'; specs=(Specs -Socket 'AM5' -FormFactor 'ATX') }
    # --- Memorias ---
    @{ sku='RAM-DDR5-32';      name='Memoria DDR5 32 GB';                           brand='Kingston';      cat='memorias-ram';    type='MEMORIA_RAM';     price=585000;  stock=6;  min=2; desc='Modulo de memoria DDR5 de 32 GB.';                                       specs=(Specs -RamType 'DDR5' -Power 12) }
    @{ sku='RAM-DDR4-16';      name='Memoria DDR4 16 GB';                           brand='Crucial';       cat='memorias-ram';    type='MEMORIA_RAM';     price=295000;  stock=8;  min=3; desc='Modulo de memoria DDR4 de 16 GB.';                                       specs=(Specs -RamType 'DDR4' -Power 9) }
    @{ sku='RAM-DDR3-8';       name='Memoria DDR3 8 GB';                            brand='Generica';      cat='memorias-ram';    type='MEMORIA_RAM';     price=95000;   stock=6;  min=2; desc='Modulo de memoria DDR3 de 8 GB, incompatible con las placas vigentes del catalogo.'; specs=(Specs -RamType 'DDR3' -Power 7) }
    # --- Fuentes ---
    @{ sku='PSU-850W-80G';     name='Fuente 850 W certificada Gold';                brand='Corsair';       cat='fuentes-de-poder'; type='FUENTE_PODER';   price=690000;  stock=5;  min=1; desc='Fuente de alimentacion de 850 W para equipos de alto consumo.';          specs=(Specs -Capacity 850) }
    @{ sku='PSU-1000W-80P';    name='Fuente 1000 W certificada Platinum';           brand='Seasonic';      cat='fuentes-de-poder'; type='FUENTE_PODER';   price=1150000; stock=3;  min=1; desc='Fuente de alimentacion de 1000 W para configuraciones exigentes.';       specs=(Specs -Capacity 1000) }
    # --- Tarjetas graficas ---
    @{ sku='GPU-RTX-4070';     name='Tarjeta grafica 12 GB';                        brand='Gigabyte';      cat='tarjetas-graficas'; type='TARJETA_GRAFICA'; price=2450000; stock=3; min=1; desc='Tarjeta grafica de gama alta para equipos de juego y edicion.';          specs=(Specs -Power 200) }
    @{ sku='GPU-RTX-4090';     name='Tarjeta grafica 24 GB de alto consumo';        brand='ASUS';          cat='tarjetas-graficas'; type='TARJETA_GRAFICA'; price=6900000; stock=2; min=1; desc='Tarjeta grafica de gama entusiasta; exige una fuente de alta capacidad.'; specs=(Specs -Power 320) }
    @{ sku='GPU-OEM-IND';      name='Tarjeta grafica sin consumo publicado';        brand='Generica';      cat='tarjetas-graficas'; type='TARJETA_GRAFICA'; price=890000; stock=2; min=1; desc='Producto de prueba que no declara su consumo, para verificar el caso no concluyente de potencia.'; specs=(Specs -Extra @{memoria='8 GB'}) }
    # --- Almacenamiento ---
    @{ sku='SSD-NVME-1TB';     name='Unidad SSD NVMe 1 TB';                         brand='Samsung';       cat='almacenamiento';  type='ALMACENAMIENTO';  price=420000;  stock=10; min=3; desc='Unidad de estado solido NVMe PCIe 4.0 de 1 TB.';                        specs=(Specs -Power 8 -Extra @{interfaz='NVMe PCIe 4.0'; capacidad='1 TB'}) }
    @{ sku='SSD-SATA-480';     name='Unidad SSD SATA 480 GB';                       brand='Kingston';      cat='almacenamiento';  type='ALMACENAMIENTO';  price=195000;  stock=12; min=4; desc='Unidad de estado solido SATA III de 480 GB.';                           specs=(Specs -Power 5 -Extra @{interfaz='SATA III'; capacidad='480 GB'}) }
    @{ sku='HDD-SATA-2TB';     name='Disco duro mecanico 2 TB';                     brand='Seagate';       cat='almacenamiento';  type='ALMACENAMIENTO';  price=310000;  stock=8;  min=2; desc='Disco duro de 2 TB a 7200 rpm para almacenamiento masivo.';             specs=(Specs -Power 9 -Extra @{interfaz='SATA III'; capacidad='2 TB'; rpm='7200'}) }
    # --- Gabinetes ---
    @{ sku='CASE-ATX-MID';     name='Gabinete de torre media ATX';                  brand='NZXT';          cat='gabinetes';       type='GABINETE';        price=380000;  stock=6;  min=2; desc='Gabinete de torre media compatible con placas ATX y microATX.';          specs=(Specs -FormFactor 'ATX' -Extra @{bahias='4'; ventiladores='2'}) }
    @{ sku='CASE-ITX-SLIM';    name='Gabinete compacto ITX';                        brand='Cooler Master'; cat='gabinetes';       type='GABINETE';        price=260000;  stock=5;  min=2; desc='Gabinete compacto para placas ITX, con espacio reducido para refrigeracion.'; specs=(Specs -FormFactor 'ITX' -Extra @{bahias='2'; ventiladores='1'}) }
    # --- Refrigeracion ---
    @{ sku='COOL-AIR-150';     name='Disipador de aire para 150 W';                 brand='Cooler Master'; cat='refrigeracion';   type='REFRIGERACION';   price=210000;  stock=8;  min=3; desc='Disipador por aire con capacidad declarada de 150 W de TDP.';           specs=(Specs -Power 4 -Extra @{tipo='aire'; tdp='150 W'}) }
    @{ sku='COOL-LIQ-240';     name='Refrigeracion liquida 240 mm';                 brand='Corsair';       cat='refrigeracion';   type='REFRIGERACION';   price=690000;  stock=4;  min=1; desc='Kit de refrigeracion liquida con radiador de 240 mm.';                   specs=(Specs -Power 12 -Extra @{tipo='liquida'; tdp='250 W'; radiador='240 mm'}) }
    # --- Accesorios (sin atributos tecnicos) ---
    @{ sku='ACC-CABLES-KIT';   name='Kit de cables modulares';                      brand='Generica';      cat='accesorios';      type='OTRO';            price=85000;   stock=15; min=5; desc='Juego de cables de alimentacion modular; no participa en ninguna regla de compatibilidad.'; specs=$null }
)

Write-Host ''
Write-Host 'Productos:'
$existentes = @{}
foreach ($prod in (Invoke-RestMethod -Uri "$CatalogoUrl/api/v1/products?includeInactive=true" -TimeoutSec 20)) { $existentes[$prod.sku] = $true }

$creados = 0; $omitidos = 0; $errores = @()
foreach ($item in $nuevos) {
    if ($existentes.ContainsKey($item.sku)) { Write-Host "  = ya existe: $($item.sku)"; $omitidos++; continue }
    $payload = @{
        sku = $item.sku; name = $item.name; brand = $item.brand
        categoryId = $mapa[$item.cat]; componentType = $item.type
        description = $item.desc; price = $item.price
        stock = $item.stock; minimumStock = $item.min; active = $true
    }
    if ($item.specs) { $payload['specs'] = $item.specs }
    try {
        $r = Send-Json -Method Post -Url "$CatalogoUrl/api/v1/products" -Token $token -Body $payload
        Write-Host "  + creado:    $($r.sku)  ($($r.componentType))"
        $creados++
    } catch {
        $detalle = $_.Exception.Message
        if ($_.ErrorDetails.Message) { $detalle = $_.ErrorDetails.Message }
        Write-Host "  ! ERROR:     $($item.sku) -> $detalle"
        $errores += $item.sku
    }
}

# --- 4. Limpieza del residuo de pruebas --------------------------------------
# CASE-ATX-E2E quedo de una prueba de extremo a extremo anterior. Los productos
# no se borran (los pedidos los referencian), se desactivan.
Write-Host ''
Write-Host 'Residuo de pruebas:'
$residuo = Invoke-RestMethod -Uri "$CatalogoUrl/api/v1/products/sku/CASE-ATX-E2E" -TimeoutSec 20 -ErrorAction SilentlyContinue
if ($residuo) {
    if ($residuo.active) {
        $null = Send-Json -Method Patch -Url "$CatalogoUrl/api/v1/products/$($residuo.id)/status?active=false" -Token $token
        Write-Host "  - desactivado: CASE-ATX-E2E (no se borra: los pedidos lo referencian)"
    } else {
        Write-Host '  = ya estaba inactivo: CASE-ATX-E2E'
    }
} else {
    Write-Host '  (no existe CASE-ATX-E2E)'
}

# --- 5. Resumen --------------------------------------------------------------
Write-Host ''
Write-Host "Productos creados : $creados"
Write-Host "Productos omitidos: $omitidos"
if ($errores.Count -gt 0) { Write-Host "Con error         : $($errores -join ', ')" }
