# verificar-compatibilidad.ps1
# Comprueba el motor de reglas contra el catalogo cargado, con los casos que el
# sistema debe saber distinguir: compatible, incompatible, no concluyente y
# falta de datos. Usa la cuenta de cliente de demostracion y borra despues las
# configuraciones que crea, para no dejar residuos.
#
# Uso: powershell -ExecutionPolicy Bypass -File C:\workspace-sts\verificar-compatibilidad.ps1

$ErrorActionPreference = 'Stop'

$UsuariosUrl = 'http://localhost:8081'
$CatalogoUrl = 'http://localhost:8082'
$CompatUrl   = 'http://localhost:8083'

function Send-Json {
    param([string]$Method, [string]$Url, [string]$Token, $Body)
    $headers = @{}
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }
    $json = if ($null -eq $Body) { '' } else { $Body | ConvertTo-Json -Depth 8 -Compress }
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers -ContentType 'application/json; charset=utf-8' -Body $bytes
}

# --- Catalog: mapa SKU -> id -------------------------------------------------
$productos = @{}
foreach ($p in (Invoke-RestMethod -Uri "$CatalogoUrl/api/v1/products?includeInactive=true" -TimeoutSec 20)) { $productos[$p.sku] = $p.id }

# --- Sesion de cliente -------------------------------------------------------
$login = Send-Json -Method Post -Url "$UsuariosUrl/api/v1/auth/login" -Body @{ email = 'cliente@pcforge.co'; password = 'Demo1234*' }
$token = if ($login.token) { $login.token } else { $login.accessToken }

$casos = @(
    @{ nombre = 'A. Armado coherente en AM5';                          skus = @('CPU-RYZEN5-7600X','MB-B650-P-WIFI','RAM-DDR5-32-6000','PSU-650W-BRONZE','GPU-RTX4060-8G','SSD-990PRO-1TB','COOL-HYPER212');             esperado = 'OK' }
    @{ nombre = 'B. Socket distinto';                                  skus = @('CPU-ULTRA7-265K','MB-B650-P-WIFI');                                                                          esperado = 'INCOMPATIBLE' }
    @{ nombre = 'C. Memoria DDR5 en placa DDR4';                       skus = @('CPU-RYZEN7-5700X','MB-B550M-PRO-VDH','RAM-DDR5-32-6000');                                                               esperado = 'INCOMPATIBLE' }
    @{ nombre = 'D. Fuente insuficiente para i9-14900K y RTX 4090';             skus = @('CPU-I9-14900K','MB-B760M-DS3H','RAM-DDR4-16-3200','GPU-RTX4090-24G','PSU-650W-BRONZE');                         esperado = 'INCOMPATIBLE' }
    @{ nombre = 'E. Grafica sin consumo declarado';                    skus = @('CPU-RYZEN5-7600X','MB-B650-P-WIFI','RAM-DDR5-32-6000','GPU-OEM-SIN-CONSUMO','PSU-650W-BRONZE');                                   esperado = 'NO_CONCLUYENTE' }
    @{ nombre = 'F. Placa sin tipos de memoria publicados';            skus = @('CPU-RYZEN5-7600X','MB-OEM-SIN-RAM','RAM-DDR5-32-6000','PSU-650W-BRONZE','GPU-RTX4060-8G');                              esperado = 'NO_CONCLUYENTE' }
    @{ nombre = 'G. Accesorio sin atributos tecnicos';                 skus = @('CPU-RYZEN5-7600X','MB-B650-P-WIFI','RAM-DDR5-32-6000','PSU-650W-BRONZE','GPU-RTX4060-8G','ACC-CABLES-MOD');                 esperado = 'OK' }
)

$creadas = @()
$fallos = 0
"{0,-46} {1,-16} {2,-16} {3}" -f 'CASO','ESPERADO','OBTENIDO','DETALLE'
'-' * 118

foreach ($caso in $casos) {
    $cfg = Send-Json -Method Post -Url "$CompatUrl/api/v1/configurations" -Token $token -Body @{ name = $caso.nombre }
    $creadas += $cfg.id

    foreach ($sku in $caso.skus) {
        $payload = @{ productId = $productos[$sku]; quantity = 1 }
        $null = Send-Json -Method Post -Url "$CompatUrl/api/v1/configurations/$($cfg.id)/items" -Token $token -Body $payload
    }

    $res = Send-Json -Method Post -Url "$CompatUrl/api/v1/configurations/$($cfg.id)/validate" -Token $token
    $obtenido = $res.result
    $ok = ($obtenido -eq $caso.esperado)
    if (-not $ok) { $fallos++ }

    $detalle = ($res.checks | ForEach-Object { "$($_.rule)=$($_.status)" }) -join ' '
    $marca = if ($ok) { '[OK]  ' } else { '[FALLA]' }
    "{0} {1,-38} {2,-16} {3,-16} {4}" -f $marca, $caso.nombre.Substring(3), $caso.esperado, $obtenido, $detalle
}

# --- Limpieza de las configuraciones de prueba -------------------------------
foreach ($id in $creadas) {
    $null = Send-Json -Method Delete -Url "$CompatUrl/api/v1/configurations/$id" -Token $token
}

''
if ($fallos -eq 0) { "Resultado: los $($casos.Count) casos dieron el veredicto esperado. Configuraciones de prueba eliminadas." }
else { "Resultado: $fallos de $($casos.Count) casos no coincidieron." }
