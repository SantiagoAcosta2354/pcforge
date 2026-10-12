# migrar-catalogo.ps1
# Actualiza el catalogo que ya existe en la base con los componentes nuevos.
#
# Por que existe: CatalogSeeder solo carga datos cuando la base esta vacia, asi
# que los cambios del seeder no llegan solos a una base que ya tiene productos.
# Este script aplica los mismos datos por API, con token de administrador.
#
# Los productos que ya existian se actualizan conservando su existencia real
# (no se pisa el stock, que puede haber cambiado con los pedidos). Los que no
# existian se crean.
#
# Uso: powershell -ExecutionPolicy Bypass -File migrar-catalogo.ps1

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\datos-catalogo.ps1"

$UsuariosUrl = 'http://localhost:8081'
$CatalogoUrl = 'http://localhost:8082'

function Enviar($metodo, $url, $token, $cuerpo) {
    $cabeceras = @{}
    if ($token) { $cabeceras['Authorization'] = "Bearer $token" }
    $parametros = @{
        Method      = $metodo
        Uri         = $url
        Headers     = $cabeceras
        ContentType = 'application/json; charset=utf-8'
        TimeoutSec  = 60
    }
    if ($null -ne $cuerpo) {
        $json = $cuerpo | ConvertTo-Json -Depth 8 -Compress
        $parametros['Body'] = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    return Invoke-RestMethod @parametros
}

# ------------------------------------------------------------------- token
Write-Host 'Autenticando como administrador...'
$login = Enviar 'Post' "$UsuariosUrl/api/v1/auth/login" $null @{ email = 'admin@pcforge.co'; password = 'PcF-b9S4sWFwZr83G-7!' }
$token = if ($login.token) { $login.token } else { $login.accessToken }
if (-not $token) { throw 'No se obtuvo token de administrador' }

# --------------------------------------------------------------- categorias
# Se lee con Invoke-WebRequest y ConvertFrom-Json en vez de con la funcion
# Enviar: al devolver un arreglo, la funcion lo entrega como un unico objeto y
# el bucle de asignacion se ejecutaria una sola vez.
$categorias = @{}
$respuesta = Invoke-WebRequest -Uri "$CatalogoUrl/api/v1/categories?includeInactive=true" -Headers @{ Authorization = "Bearer $token" } -UseBasicParsing -TimeoutSec 60
foreach ($cat in ($respuesta.Content | ConvertFrom-Json)) { $categorias[$cat.slug] = $cat.id }
Write-Host "Categorias encontradas: $($categorias.Count)"

$items = Get-CatalogoPCForge
$actualizados = 0
$creados = 0
$errores = @()

foreach ($it in $items) {
    $categoriaId = $categorias[$it.cat]
    if (-not $categoriaId) { $errores += "sin categoria: $($it.sku)"; continue }

    # specs exactas: solo los campos que el componente declara
    $specs = $null
    if ($it.socket -or $it.ramSup -or $it.ramType -or $it.powerW -or $it.capW -or $it.form -or $it.extra) {
        $specs = @{}
        if ($it.socket)  { $specs['socket'] = $it.socket }
        if ($it.ramSup)  { $specs['ramTypeSupported'] = @($it.ramSup) }
        if ($it.ramType) { $specs['ramType'] = $it.ramType }
        if ($it.powerW)  { $specs['estimatedPowerW'] = $it.powerW }
        if ($it.capW)    { $specs['capacityW'] = $it.capW }
        if ($it.form)    { $specs['formFactor'] = $it.form }
        if ($it.extra)   { $specs['extra'] = @{} ; foreach ($k in $it.extra.Keys) { $specs['extra'][$k] = $it.extra[$k] } }
    }

    $cuerpo = @{
        sku           = $it.sku
        name          = $it.name
        brand         = $it.brand
        categoryId    = $categoriaId
        componentType = $it.tipo
        description   = $it.desc
        price         = [double]$it.price
        stock         = [int]$it.stock
        minimumStock  = [int]$it.min
        active        = $true
    }
    if ($specs) { $cuerpo['specs'] = $specs }

    # localizar el producto existente: por el SKU viejo o, si es nuevo, por el suyo
    $existente = $null
    foreach ($buscar in @($it.viejo, $it.sku)) {
        if (-not $buscar) { continue }
        try {
            $existente = Enviar 'Get' "$CatalogoUrl/api/v1/products/sku/$buscar" $token $null
            break
        } catch { }
    }

    if ($existente) {
        # se conserva la existencia real del producto
        $cuerpo['stock'] = [int]$existente.stock
        $cuerpo['active'] = [bool]$existente.active
        Enviar 'Put' "$CatalogoUrl/api/v1/products/$($existente.id)" $token $cuerpo | Out-Null
        $actualizados++
        Write-Host "  actualizado  $($it.sku)  <- $($existente.sku)"
    } else {
        Enviar 'Post' "$CatalogoUrl/api/v1/products" $token $cuerpo | Out-Null
        $creados++
        Write-Host "  creado       $($it.sku)"
    }
}

Write-Host ''
Write-Host "Actualizados: $actualizados"
Write-Host "Creados     : $creados"
if ($errores.Count -gt 0) {
    Write-Host "Errores     : $($errores.Count)"
    $errores | ForEach-Object { Write-Host "   $_" }
}
