# arrancar-pcforge.ps1
# Arranca los cinco microservicios de PCForge en local (8080-8084).
#
# Punto clave: cada servicio se lanza con su PROPIO directorio como carpeta de
# trabajo. Spring Boot busca la configuracion en ./config/application.properties
# con mayor prioridad que la que va dentro del jar, y ahi es donde esta la
# cadena de conexion a MongoDB Atlas y el secreto JWT compartido.
# Por eso este script no contiene credenciales: cada modulo aporta las suyas.
#
# Uso:  powershell -ExecutionPolicy Bypass -File C:\workspace-sts\arrancar-pcforge.ps1

$ErrorActionPreference = 'Stop'

$WorkspaceRoot = 'C:\workspace-sts'
$LogDir        = Join-Path $env:TEMP 'pcforge-run'

# Nombre del modulo, puerto HTTP y si necesita configuracion externa (Mongo/secretos).
$services = @(
    [pscustomobject]@{ Name = 'pcforge-usuarios';       Port = 8081; NeedsExternalConfig = $true }
    [pscustomobject]@{ Name = 'pcforge-catalogo';       Port = 8082; NeedsExternalConfig = $true }
    [pscustomobject]@{ Name = 'pcforge-compatibilidad'; Port = 8083; NeedsExternalConfig = $true }
    [pscustomobject]@{ Name = 'pcforge-pedidos';        Port = 8084; NeedsExternalConfig = $true }
    [pscustomobject]@{ Name = 'pcforge-web';            Port = 8080; NeedsExternalConfig = $false }
)

$javaExe = Join-Path $env:JAVA_HOME 'bin\java.exe'
if (-not (Test-Path $javaExe)) { $javaExe = 'java' }

if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }

# --- 1. Comprobar configuracion externa --------------------------------------
foreach ($svc in $services) {
    if (-not $svc.NeedsExternalConfig) { continue }
    $cfg = Join-Path $WorkspaceRoot "$($svc.Name)\config\application.properties"
    if (-not (Test-Path $cfg)) {
        Write-Warning "$($svc.Name): falta $cfg. Sin ese archivo el servicio intentara MongoDB en localhost y fallara."
    }
}

# --- 2. Arrancar -------------------------------------------------------------
Write-Host 'Arrancando servicios...'
foreach ($svc in $services) {

    $moduleDir = Join-Path $WorkspaceRoot $svc.Name
    $jar = Get-ChildItem (Join-Path $moduleDir 'target\*.jar') -ErrorAction SilentlyContinue |
           Where-Object { $_.Name -notlike '*sources*' -and $_.Name -notlike '*original*' } |
           Select-Object -First 1

    if (-not $jar) { Write-Warning "$($svc.Name): no hay JAR compilado, se omite. Ejecute 'mvn clean package' primero."; continue }

    $outLog = Join-Path $LogDir "$($svc.Name).out.log"
    $errLog = Join-Path $LogDir "$($svc.Name).err.log"

    $proc = Start-Process -FilePath $javaExe `
                          -ArgumentList @('-jar', $jar.FullName, "--server.port=$($svc.Port)") `
                          -WorkingDirectory $moduleDir `
                          -RedirectStandardOutput $outLog -RedirectStandardError $errLog `
                          -WindowStyle Hidden -PassThru

    Write-Host ("  [OK] {0,-22} puerto {1}  PID {2}" -f $svc.Name, $svc.Port, $proc.Id)
}

# --- 3. Esperar a que respondan ---------------------------------------------
Write-Host ''
Write-Host 'Esperando a que los servicios respondan...'
$pendientes = @()
foreach ($svc in $services) {
    $url = "http://localhost:$($svc.Port)/actuator/health"
    $ok = $false
    for ($i = 1; $i -le 25; $i++) {
        try {
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3
            if ($r.StatusCode -eq 200) { $ok = $true; break }
        } catch { Start-Sleep -Seconds 2 }
    }
    if ($ok) { Write-Host ("  [UP]   {0,-22} http://localhost:{1}" -f $svc.Name, $svc.Port) }
    else     { Write-Host ("  [DOWN] {0,-22} {1}" -f $svc.Name, (Join-Path $LogDir "$($svc.Name).out.log")); $pendientes += $svc.Name }
}

Write-Host ''
if ($pendientes.Count -eq 0) {
    Write-Host 'Los cinco servicios responden.'
    Write-Host 'Interfaz web:  http://localhost:8080/login'
} else {
    Write-Host "Servicios que no respondieron: $($pendientes -join ', ')"
}
Write-Host "Logs: $LogDir"
