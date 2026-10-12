# ---------------------------------------------------------------------------
# Prueba de la capa web de PCForge
#
# Levanta los cinco procesos (los cuatro microservicios y la capa web) y
# recorre el sistema como lo haria una persona: navega el catalogo sin sesion,
# inicia sesion, arma un equipo con una memoria equivocada, ve el resultado
# incompatible, corrige el componente y obtiene un resultado compatible,
# registra la solicitud, y recorre el ciclo completo con el asesor, el tecnico
# y el administrador, comprobando en cada paso los limites de permisos.
#
# Requisitos: los cinco proyectos compilados (mvn package) y la configuracion
# local en config/application.properties de cada uno.
# Ejecutar con:  powershell -File pruebas\prueba-capa-web.ps1
# ---------------------------------------------------------------------------
$ErrorActionPreference = 'Continue'
$s = 'C:\workspace-sts'
$mods = @('pcforge-usuarios','pcforge-catalogo','pcforge-compatibilidad','pcforge-pedidos','pcforge-web')
$procs = @()
foreach ($m in $mods) {
  $d = "$s\$m"
  $procs += Start-Process java -ArgumentList '-jar',"target\$m-0.0.1-SNAPSHOT.jar" `
    -RedirectStandardOutput "$d\target\web-e2e.log" -RedirectStandardError "$d\target\web-e2e.err" `
    -PassThru -NoNewWindow -WorkingDirectory $d
}
"Servicios lanzados. Esperando el arranque..."
Start-Sleep -Seconds 95

$base = 'http://localhost:8080'
function Csrf($url, $sesion) {
  $html = (Invoke-WebRequest -Uri $url -WebSession $sesion -UseBasicParsing -TimeoutSec 30).Content
  $m = [regex]::Match($html, 'name="_csrf"\s+value="([^"]+)"')
  if (-not $m.Success) { throw "No se encontro el campo CSRF en $url" }
  return $m.Groups[1].Value
}
function Pagina($url, $sesion) {
  return (Invoke-WebRequest -Uri $url -WebSession $sesion -UseBasicParsing -TimeoutSec 30).Content
}
function Entrar($email, $clave) {
  $sesion = New-Object Microsoft.PowerShell.Commands.WebRequestSession
  $token = Csrf "$base/login" $sesion
  Invoke-WebRequest -Uri "$base/login" -Method Post -WebSession $sesion -UseBasicParsing -TimeoutSec 30 `
    -Body @{ username = $email; password = $clave; _csrf = $token } | Out-Null
  return $sesion
}
# Envia un formulario y devuelve el contenido de la pagina destino,
# aprovechando el atributo flash que deja la accion.
function EnviarFormulario($ruta, $cuerpo, $sesion, $paginaCsrf) {
  $token = Csrf "$base$paginaCsrf" $sesion
  $cuerpo['_csrf'] = $token
  return (Invoke-WebRequest -Uri "$base$ruta" -Method Post -WebSession $sesion -UseBasicParsing `
    -TimeoutSec 60 -Body $cuerpo).Content
}

try {
  # ---------------- navegacion publica ----------------
  $anon = New-Object Microsoft.PowerShell.Commands.WebRequestSession
  $inicio = Pagina "$base/" $anon
  "[1] Portada publica sin sesion -> HTTP 200: $($inicio -match 'PCForge') | eslogan: $($inicio -match 'sin comprar piezas equivocadas')"

  $catalogo = Pagina "$base/catalogo" $anon
  "[2] Catalogo publico -> HTTP 200 | muestra productos: $($catalogo -match 'Procesador')"

  $ids = @{}
  foreach ($sku in @('CPU-AM5-7600','MB-AM5-DDR5','RAM-DDR5-16','PSU-650W-80P','GPU-RTX-4060','RAM-DDR4-8','PSU-450W-BAS')) {
    $ids[$sku] = (Invoke-RestMethod "http://localhost:8082/api/v1/products/sku/$sku" -TimeoutSec 20).id
  }
  "[3] Productos del catalogo resueltos: $($ids.Count)"

  # ---------------- sesion del cliente ----------------
  $cli = Entrar 'cliente@pcforge.co' 'Demo1234*'
  "[4] Inicio de sesion -> la barra muestra el nombre: $((Pagina "$base/" $cli) -match 'Cliente de prueba')"
  "[5] Pantalla de armado vacia -> $((Pagina "$base/armar" $cli) -match 'Todavia no ha agregado componentes')"

  # Equipo con una memoria equivocada a proposito: placa DDR5 con modulo DDR4
  foreach ($sku in @('CPU-AM5-7600','MB-AM5-DDR5','RAM-DDR4-8','PSU-450W-BAS')) {
    EnviarFormulario '/armar/agregar' @{ productId = $ids[$sku]; cantidad = 1 } $cli '/armar' | Out-Null
  }
  $armado = Pagina "$base/armar" $cli
  "[6] Componentes en el equipo: $(([regex]::Matches($armado, 'name="itemId"')).Count)"

  $validado = EnviarFormulario '/armar/validar' @{} $cli '/armar'
  "[7] Validacion de un equipo incoherente -> muestra 'Incompatible': $($validado -match 'Incompatible')"
  "[8]   la regla de memoria explica el motivo: $($validado -match 'es DDR4 y la placa admite')"
  "[9]   la regla de potencia tambien se evaluo: $($validado -match 'Potencia')"

  # Se retira la memoria equivocada y se pone la correcta
  $itemDdr4 = [regex]::Match($armado, 'RAM-DDR4-8[\s\S]{0,2500}?name="itemId"\s+value="([^"]+)"').Groups[1].Value
  if (-not $itemDdr4) { throw "No se pudo identificar el componente DDR4 para retirarlo" }
  EnviarFormulario '/armar/quitar' @{ itemId = $itemDdr4 } $cli '/armar' | Out-Null
  EnviarFormulario '/armar/agregar' @{ productId = $ids['RAM-DDR5-16']; cantidad = 1 } $cli '/armar' | Out-Null

  $validado2 = EnviarFormulario '/armar/validar' @{} $cli '/armar'
  "[10] Tras corregir la memoria -> muestra 'Compatible': $($validado2 -match 'Compatible') | sigue incompatible: $($validado2 -match 'Incompatible')"

  # ---------------- registro de la solicitud ----------------
  $detalle = EnviarFormulario '/armar/pedido' @{ observacion = 'Solicitud creada desde la prueba de la capa web' } $cli '/armar'
  $numero = [regex]::Match($detalle, '(PF-\d{6})').Groups[1].Value
  "[11] Solicitud registrada desde la web -> $numero | estado Recibido: $($detalle -match 'Recibido') | muestra el historial: $($detalle -match 'Historial de estados')"

  $listaCliente = Pagina "$base/pedidos" $cli
  $rutaPedido = [regex]::Match($listaCliente, '/pedidos/[a-f0-9]{24}').Value
  $idPedido = $rutaPedido -replace '/pedidos/', ''
  "[12] Aparece en la lista del cliente: $($listaCliente -match $numero)"

  # ---------------- limites de permisos ----------------
  try {
    Invoke-WebRequest -Uri "$base/inventario" -WebSession $cli -UseBasicParsing -TimeoutSec 30 | Out-Null
    "[13] ERROR: el cliente entro al inventario"
  } catch {
    $codigo = $_.Exception.Response.StatusCode.value__
    $cuerpo = (New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd()
    "[13] Cliente abriendo /inventario -> HTTP $codigo | pantalla de acceso denegado: $($cuerpo -match 'no tiene acceso')"
  }

  # ---------------- sesion del asesor ----------------
  $ase = Entrar 'asesor@pcforge.co' 'Demo1234*'
  $listaAsesor = Pagina "$base/pedidos" $ase
  "[14] El asesor ve los pedidos de todos -> aparece $numero : $($listaAsesor -match $numero) | ve el nombre del cliente: $($listaAsesor -match 'Cliente de prueba')"

  $detalleAsesor = Pagina "$base$rutaPedido" $ase
  "[15] Detalle del pedido para el asesor -> ofrece cambiar estado: $($detalleAsesor -match 'Cambiar el estado') | puede asignar tecnico: $($detalleAsesor -match 'Tecnico que asume el ensamblaje')"

  foreach ($estado in @('EN_REVISION','COTIZADO','EN_ENSAMBLAJE')) {
    EnviarFormulario "/pedidos/$idPedido/estado" @{ status = $estado; note = "Cambio a $estado desde la web"; technicianId = ''; technicianName = '' } $ase "$rutaPedido" | Out-Null
  }
  $trasEstados = Pagina "$base$rutaPedido" $ase
  "[16] Recorrido comercial desde la web -> queda 'En ensamblaje': $($trasEstados -match 'En ensamblaje')"
  "[17]   el aviso explica que se desconto el inventario: $($trasEstados -match 'se descuentan las existencias')"

  # ---------------- sesion del tecnico ----------------
  $tec = Entrar 'tecnico@pcforge.co' 'Demo1234*'
  foreach ($estado in @('EN_PRUEBAS','LISTO_PARA_ENTREGA')) {
    EnviarFormulario "/pedidos/$idPedido/estado" @{ status = $estado; note = "Etapa $estado"; technicianId = ''; technicianName = '' } $tec "$rutaPedido" | Out-Null
  }
  EnviarFormulario "/pedidos/$idPedido/observacion" @{ text = 'Equipo armado y probado sin novedades' } $tec "$rutaPedido" | Out-Null
  $trasTecnico = Pagina "$base$rutaPedido" $tec
  "[18] Etapas del tecnico -> 'Listo para entrega': $($trasTecnico -match 'Listo para entrega') | su observacion quedo registrada: $($trasTecnico -match 'sin novedades')"
  "[19]   el tecnico no puede confirmar la cotizacion: $(-not ($trasTecnico -match 'value=\"COTIZADO\"'))"

  # ---------------- sesion del administrador ----------------
  $adm = Entrar 'admin@pcforge.co' 'Admin1234*'
  $inv = Pagina "$base/inventario" $adm
  "[20] Inventario para el administrador -> HTTP 200 | tabla de existencias: $($inv -match 'Existencia') | alertas de minimo: $($inv -match 'existencia minima')"

  $antes = (Invoke-RestMethod 'http://localhost:8082/api/v1/products/sku/RAM-DDR4-8' -TimeoutSec 20).stock
  EnviarFormulario "/inventario/$($ids['RAM-DDR4-8'])/movimiento" @{ movementType = 'ENTRADA'; quantity = 7; reason = 'Compra a proveedor' } $adm '/inventario' | Out-Null
  $despues = (Invoke-RestMethod 'http://localhost:8082/api/v1/products/sku/RAM-DDR4-8' -TimeoutSec 20).stock
  "[21] Movimiento desde la web -> existencia de RAM-DDR4-8: $antes -> $despues (esperado $($antes + 7))"

} catch {
  "FALLO: $($_.Exception.Message)"
  if ($_.ErrorDetails.Message) { "DETALLE: " + $_.ErrorDetails.Message.Substring(0, [Math]::Min(500, $_.ErrorDetails.Message.Length)) }
}

foreach ($p in $procs) { if (-not $p.HasExited) { Stop-Process -Id $p.Id -Force } }

