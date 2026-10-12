# pcforge-pedidos

Microservicio de **cotizaciones, pedidos y seguimiento del ensamblaje** de
PCForge. Registra la solicitud a partir de una configuracion armada y controla
el avance del proceso.

- Java 21 · Spring Boot 4.1.1 · Spring Data MongoDB · Spring Security (JWT)
- Base de datos propia: `pcforge_pedidos`
- Se integra por API con `catalogo` (8082) y `compatibilidad` (8083)

## Flujo de estados

```
RECIBIDO -> EN_REVISION -> COTIZADO -> EN_ENSAMBLAJE -> EN_PRUEBAS
         -> LISTO_PARA_ENTREGA -> ENTREGADO
```

Desde cualquier estado abierto se puede pasar a `CANCELADO`.

Las transiciones no son libres y cada una exige un perfil:

| Estado destino | Quien puede |
|---|---|
| `EN_REVISION`, `COTIZADO`, `EN_ENSAMBLAJE`, `ENTREGADO` | Asesor comercial o administrador |
| `EN_PRUEBAS`, `LISTO_PARA_ENTREGA` | Tecnico de ensamblaje o administrador |
| `CANCELADO` | Asesor, administrador, o el propio cliente mientras la solicitud este en `RECIBIDO` o `EN_REVISION` |

## Reglas de negocio implementadas

1. **La disponibilidad se vuelve a comprobar al registrar la solicitud**, porque
   lo que se vio en pantalla pudo cambiar entre el armado y el registro. Si no
   alcanzan las existencias, la solicitud no se crea y se indica que componente
   y cuantas unidades faltan.
2. **Los precios se toman del catalogo**, nunca del cuerpo de la peticion.
3. **Al entrar a `EN_ENSAMBLAJE` se descuentan las existencias** llamando al
   catalogo. Si el descuento falla, el pedido **no avanza** y se devuelven los
   componentes que ya se habian descontado.
4. **Cada componente y cada precio quedan copiados en el pedido**, de modo que
   una solicitud antigua sigue siendo explicable aunque el catalogo cambie.
5. **El historial de estados nunca se sobrescribe**: guarda estado anterior,
   estado nuevo, quien lo cambio, cuando y con que observacion.
6. **Los numeros de pedido son consecutivos y unicos**, generados con una
   operacion atomica en MongoDB.
7. **Solo el asesor comercial o el administrador asignan el tecnico** de
   ensamblaje; cualquier otro perfil recibe 403.
8. **Un pedido cerrado no admite observaciones**: una vez en `ENTREGADO` o
   `CANCELADO` el historial ya no se modifica.

## Variables de entorno

| Variable | Para que sirve | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP | `8084` |
| `PCFORGE_MONGO_URI` | Cadena de conexion a MongoDB | `mongodb://localhost:27017/pcforge_pedidos` |
| `PCFORGE_JWT_SECRET` | Secreto compartido para validar el JWT | valor de desarrollo |
| `PCFORGE_CATALOGO_URL` | Direccion del microservicio de catalogo | `http://localhost:8082` |
| `PCFORGE_COMPATIBILIDAD_URL` | Direccion del microservicio de compatibilidad | `http://localhost:8083` |
| `PCFORGE_ORDER_PREFIX` | Prefijo de los numeros de pedido | `PF` |
| `PCFORGE_DOWNSTREAM_TIMEOUT_MS` | Tiempo de espera al consultar catalogo y compatibilidad | `6000` |

## Endpoints

| Metodo | Ruta | Acceso | Descripcion |
|---|---|---|---|
| POST | `/api/v1/orders` | Autenticado | Registra una solicitud desde una configuracion |
| GET | `/api/v1/orders` | Autenticado | Pedidos propios; el personal ve todos. Filtro `status` |
| GET | `/api/v1/orders/{id}` | Dueño o personal | Acepta el id interno o el numero `PF-000001` |
| PATCH | `/api/v1/orders/{id}/status` | Segun transicion | Cambia el estado |
| POST | `/api/v1/orders/{id}/observations` | Dueño o personal | Agrega una observacion |

La respuesta incluye `allowedNextStatuses`, de modo que la interfaz no tenga
que repetir las reglas de transicion.

## Colecciones

- `orders` — pedido con numero consecutivo, dueño, componentes copiados,
  historial de estados y observaciones
- `counters` — contador de la numeracion consecutiva, actualizado con una
  operacion atomica

## Nota sobre el token

Este servicio **reenvia el token del usuario** a los otros microservicios en
lugar de usar credenciales propias. Esa decision es deliberada: la
configuracion y los pedidos tienen dueño, y si este servicio consultara con
credenciales de administrador podria leer datos de cualquier cliente. Al
reenviar el token, la verificacion de propiedad la sigue haciendo el servicio
que es dueño del dato.

## Pruebas

```bash
mvn clean test
```

Doce pruebas: precios y registro, disponibilidad insuficiente, configuracion
vacia, transiciones invalidas, permisos por perfil, anulacion por el cliente,
descuento de inventario, fallo de inventario sin avance de estado, y control de
acceso entre clientes.
