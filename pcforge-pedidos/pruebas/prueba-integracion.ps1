# ---------------------------------------------------------------------------
# Prueba de integracion de PCForge
#
# Levanta los cuatro microservicios y recorre el flujo completo: el cliente
# arma una configuracion y registra un pedido, el asesor lo cotiza, el tecnico
# lo ensambla y lo prueba, y el administrador cierra la entrega. Verifica
# ademas que el inventario del catalogo se descuente al entrar en ensamblaje y
# que se rechacen las operaciones no autorizadas.
#
# Requisitos: los cuatro proyectos compilados (mvn package) y un archivo
# config/application.properties en cada uno con la cadena de MongoDB y el
# secreto JWT. Se ejecuta con:  powershell -File pruebas\prueba-integracion.ps1
# ---------------------------------------------------------------------------
$ErrorActionPreference = 'Continue'
$s = 'C:\workspace-sts'
$mods = @('pcforge-usuarios','pcforge-catalogo','pcforge-compatibilidad','pcforge-pedidos')
$procs = @()
foreach ($m in $mods) {
  $d = "$s\$m"
  $procs += Start-Process java -ArgumentList '-jar',"target\$m-0.0.1-SNAPSHOT.jar" `
    -RedirectStandardOutput "$d\target\e2e.log" -RedirectStandardError "$d\target\e2e.err" `
    -PassThru -NoNewWindow -WorkingDirectory $d
}
Start-Sleep -Seconds 70

function Login($email, $pass) {
  (Invoke-RestMethod 'http://localhost:8081/api/v1/auth/login' -Method Post -ContentType 'application/json' `
    -Body (@{email=$email;password=$pass} | ConvertTo-Json) -TimeoutSec 20).accessToken
}
function Head($t) { @{ Authorization = "Bearer $t" } }
function NewConfig($t, $n) { (Invoke-RestMethod 'http://localhost:8083/api/v1/configurations' -Method Post -ContentType 'application/json' -Headers (Head $t) -Body (@{name=$n}|ConvertTo-Json) -TimeoutSec 20).id }
function AddItem($t, $c, $prodId, $qty) { Invoke-RestMethod "http://localhost:8083/api/v1/configurations/$c/items" -Method Post -ContentType 'application/json' -Headers (Head $t) -Body (@{productId=$prodId;quantity=$qty}|ConvertTo-Json) -TimeoutSec 20 | Out-Null }
function Validar($t, $c) { Invoke-RestMethod "http://localhost:8083/api/v1/configurations/$c/validate" -Method Post -Headers (Head $t) -TimeoutSec 20 }
function Producto($sku) { Invoke-RestMethod "http://localhost:8082/api/v1/products/sku/$sku" -TimeoutSec 20 }
function Cambiar($t, $o, $estado, $nota) { Invoke-RestMethod "http://localhost:8084/api/v1/orders/$o/status" -Method Patch -ContentType 'application/json' -Headers (Head $t) -Body (@{status=$estado;note=$nota}|ConvertTo-Json) -TimeoutSec 20 }
function Detalle($e) { $r = New-Object System.IO.StreamReader($e.Exception.Response.GetResponseStream()); $r.ReadToEnd() }

try {
  $cli = Login 'cliente@pcforge.co' 'Demo1234*'
  $ase = Login 'asesor@pcforge.co' 'Demo1234*'
  $tec = Login 'tecnico@pcforge.co' 'Demo1234*'
  "TOKENS obtenidos: cliente, asesor y tecnico"

  $ids = @{}
  foreach ($sku in @('CPU-AM5-7600','MB-AM5-DDR5','RAM-DDR5-16','PSU-650W-80P','GPU-RTX-4060')) { $ids[$sku] = (Producto $sku).id }

  $cfg = NewConfig $cli 'Equipo para la demostracion'
  foreach ($k in @('CPU-AM5-7600','MB-AM5-DDR5','RAM-DDR5-16','PSU-650W-80P','GPU-RTX-4060')) { AddItem $cli $cfg $ids[$k] 1 }
  $val = Validar $cli $cfg
  "[1] Validacion en compatibilidad -> $($val.result)"

  $o = Invoke-RestMethod 'http://localhost:8084/api/v1/orders' -Method Post -ContentType 'application/json' -Headers (Head $cli) -Body (@{configurationId=$cfg;customerNote='Listo para comprar'}|ConvertTo-Json) -TimeoutSec 20
  "[2] Pedido $($o.orderNumber) | estado $($o.status) | total $($o.total) | $($o.items.Count) componentes | inventario descontado: $($o.stockDiscounted)"

  $antes = (Producto 'GPU-RTX-4060').stock
  "[3] Existencia de la tarjeta grafica antes del ensamblaje: $antes"

  $a1 = Cambiar $ase $o.id 'EN_REVISION' 'Tomada para revision'
  $a2 = Cambiar $ase $o.id 'COTIZADO' 'Cotizacion enviada al cliente'
  $a3 = Cambiar $ase $o.id 'EN_ENSAMBLAJE' 'Cliente acepto la cotizacion'
  "[4] Fase comercial -> $($a1.status) -> $($a2.status) -> $($a3.status) | inventario descontado: $($a3.stockDiscounted)"

  $despues = (Producto 'GPU-RTX-4060').stock
  "[5] Existencia despues del ensamblaje: $despues (esperado $($antes - 1))"

  $a4 = Cambiar $tec $o.id 'EN_PRUEBAS' 'Equipo armado, entra a pruebas'
  $a5 = Cambiar $tec $o.id 'LISTO_PARA_ENTREGA' 'Pruebas de encendido superadas'
  "[6] Fase de ensamblaje -> $($a4.status) -> $($a5.status)"

  $obs = Invoke-RestMethod "http://localhost:8084/api/v1/orders/$($o.id)/observations" -Method Post -ContentType 'application/json' -Headers (Head $tec) -Body (@{text='Se verifico el armado y las pruebas de encendido'}|ConvertTo-Json) -TimeoutSec 20
  "[7] Observacion del tecnico registrada (total $($obs.observations.Count))"

  $a6 = Cambiar $ase $o.id 'ENTREGADO' 'Entregado al cliente'
  "[8] Cierre -> $($a6.status) | fecha de entrega registrada: $($a6.deliveredAt -ne $null) | cambios en el historial: $($a6.history.Count)"
} catch { "FALLO EN EL FLUJO PRINCIPAL: $($_.Exception.Message)" }

# --- Verificaciones de reglas, cada una independiente ---
try {
  $cli = Login 'cliente@pcforge.co' 'Demo1234*'
  $cfg2 = NewConfig $cli 'Segunda prueba'
  AddItem $cli $cfg2 $ids['CPU-AM5-7600'] 1
  $o2 = Invoke-RestMethod 'http://localhost:8084/api/v1/orders' -Method Post -ContentType 'application/json' -Headers (Head $cli) -Body (@{configurationId=$cfg2}|ConvertTo-Json) -TimeoutSec 20
  try { Cambiar $cli $o2.id 'EN_REVISION' 'intento' | Out-Null; "[9] ERROR: el cliente pudo tomar su solicitud" }
  catch { "[9] Cliente tomando su propia solicitud -> HTTP $($_.Exception.Response.StatusCode.value__) (403 esperado)" }
} catch { "[9] no se pudo verificar: $($_.Exception.Message)" }

try {
  $ajeno = Login 'ajeno@pcforge.co' 'ClaveAjena123'
  Invoke-RestMethod "http://localhost:8084/api/v1/orders/$($o.id)" -Headers (Head $ajeno) -TimeoutSec 20 | Out-Null
  "[10] ERROR: otro cliente vio el pedido"
} catch { "[10] Otro cliente consultando el pedido ajeno -> HTTP $($_.Exception.Response.StatusCode.value__) (403 esperado)" }

try {
  $cli = Login 'cliente@pcforge.co' 'Demo1234*'
  $tecn = Login 'tecnico@pcforge.co' 'Demo1234*'
  Invoke-RestMethod "http://localhost:8084/api/v1/orders/$($o.id)/observations" -Method Post -ContentType 'application/json' -Headers (Head $tecn) -Body (@{text='Otra nota'}|ConvertTo-Json) -TimeoutSec 20 | Out-Null
  "[11] ERROR: acepto una observacion en un pedido entregado"
} catch { "[11] Observacion sobre pedido entregado -> HTTP $($_.Exception.Response.StatusCode.value__) (409 esperado)" }

try {
  $cli = Login 'cliente@pcforge.co' 'Demo1234*'
  $stockActual = (Producto 'GPU-RTX-4060').stock
  $cfg3 = NewConfig $cli 'Tercera prueba'
  AddItem $cli $cfg3 $ids['GPU-RTX-4060'] 5
  Invoke-RestMethod 'http://localhost:8084/api/v1/orders' -Method Post -ContentType 'application/json' -Headers (Head $cli) -Body (@{configurationId=$cfg3}|ConvertTo-Json) -TimeoutSec 20 | Out-Null
  "[12] ERROR: acepto un pedido sin existencias"
} catch {
  "[12] Pedido de 5 tarjetas con existencia de $stockActual -> HTTP $($_.Exception.Response.StatusCode.value__) (409 esperado)"
  $d = Detalle $_
  if ($d) { "     " + (($d | ConvertFrom-Json).message) }
}

foreach ($p in $procs) { if (-not $p.HasExited) { Stop-Process -Id $p.Id -Force } }

