# Genera un JWT de desarrollo firmado con el secreto compartido, para probar
# los roles sin depender del microservicio de usuarios.
#
# Uso:
#   .\scripts\dev-token.ps1 -Roles CURADOR
#   .\scripts\dev-token.ps1 -Roles ANALISTA -Call "http://localhost:8082/api/v1/pricing/margins?onlyAlerts=true"
#
# El secreto se toma de la variable de entorno PCFORGE_JWT_SECRET. Si no esta
# definida se usa el valor de desarrollo que trae application.properties, que
# nunca debe usarse fuera de local.

param(
    [Parameter(Mandatory = $true)][string[]]$Roles,
    [string]$Subject = "demo-local",
    [int]$Hours = 2,
    [string]$Secret = $(if ($env:PCFORGE_JWT_SECRET) { $env:PCFORGE_JWT_SECRET } else { "pcforge-clave-de-desarrollo-cambiar-en-produccion-2026" }),
    [string]$Call
)

function ConvertTo-Base64Url([byte[]]$bytes) {
    [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

$header = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes('{"alg":"HS256","typ":"JWT"}'))
$exp = [DateTimeOffset]::UtcNow.AddHours($Hours).ToUnixTimeSeconds()
$payloadJson = @{ sub = $Subject; roles = $Roles; exp = $exp } | ConvertTo-Json -Compress
$payload = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($payloadJson))
$unsigned = "$header.$payload"

$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($Secret)
$signature = ConvertTo-Base64Url ($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($unsigned)))
$token = "$unsigned.$signature"

if ($Call) {
    $response = Invoke-RestMethod -Uri $Call -Headers @{ Authorization = "Bearer $token" } -TimeoutSec 30
    $response | ConvertTo-Json -Depth 6
} else {
    $token
}
