# pcforge-catalogo

Microservicio de **catalogo e inventario** de PCForge. Administra productos,
categorias, atributos tecnicos, precios y existencias. Es la fuente de verdad
que consultan el validador de compatibilidad y el modulo de pedidos **a traves
de su API REST**, nunca por acceso directo a esta base de datos.

- Java 21 · Spring Boot 4.1.1 · Spring Data MongoDB · Spring Security (JWT)
- Base de datos propia: `pcforge_catalogo` (patron database-per-service)
- Documentacion de la API: `/swagger-ui.html` y `/v3/api-docs`

## Puertos y variables de entorno

| Variable | Para que sirve | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP del servicio | `8082` |
| `PCFORGE_MONGO_URI` | Cadena de conexion a MongoDB | `mongodb://localhost:27017/pcforge_catalogo` |
| `PCFORGE_JWT_SECRET` | Secreto compartido para validar el JWT (**minimo 32 caracteres**) | valor de desarrollo |
| `PCFORGE_SEED_ENABLED` | Carga los datos de demostracion si la base esta vacia | `true` |
| `PCFORGE_MARGEN_MINIMO` | Margen minimo global cuando la categoria no tiene politica propia (porcentaje sobre el precio) | `18` |
| `PCFORGE_UMBRAL_DESCUENTO` | Descuento maximo sin aprobacion (porcentaje) | `5` |

En produccion **no** se usan los valores por defecto: el secreto y la cadena de
conexion se configuran como variables de entorno en la plataforma.

## Como ejecutarlo en local

```bash
export PCFORGE_MONGO_URI="mongodb+srv://usuario:clave@cluster/pcforge_catalogo"
export PCFORGE_JWT_SECRET="una-clave-larga-de-al-menos-32-caracteres"
mvn spring-boot:run
```

## Endpoints principales

| Metodo | Ruta | Acceso | Descripcion |
|---|---|---|---|
| GET | `/api/v1/products` | Publico | Lista o busca productos (filtros `categoryId`, `componentType`, `name` e `includeInactive`) |
| GET | `/api/v1/products/{id}` | Publico | Consulta un producto |
| GET | `/api/v1/products/sku/{sku}` | Publico | Consulta por SKU |
| GET | `/api/v1/products/low-stock` | Publico | Productos que alcanzaron su minimo |
| GET | `/api/v1/products/{id}/stock/movements` | Publico | Historial de movimientos |
| POST | `/api/v1/products` | ADMIN | Registra un producto |
| PUT | `/api/v1/products/{id}` | ADMIN | Actualiza un producto |
| PATCH | `/api/v1/products/{id}/status?active=` | ADMIN | Activa o desactiva |
| POST | `/api/v1/products/{id}/stock` | ADMIN / ASESOR / TECNICO | Entrada, salida o ajuste de existencia |
| GET | `/api/v1/categories` | Publico | Lista de categorias |
| GET | `/api/v1/categories/slug/{slug}` | Publico | Consulta una categoria por su slug |
| POST / PUT / PATCH | `/api/v1/categories/**` | ADMIN | Mantenimiento de categorias |

### Curador de datos tecnicos (`CURADOR`)

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/v1/catalog-quality/pending` | Bandeja de fichas incompletas (`includeInactive`, `includeUnverified`) |
| GET | `/api/v1/catalog-quality/products/{id}` | Ficha tecnica con su trazabilidad y atributos pendientes |
| PUT | `/api/v1/catalog-quality/products/{id}/attributes` | Registra atributos y su fuente; permite declarar la ficha completa |
| GET | `/api/v1/catalog-quality/coverage` | Porcentaje de fichas completas y atributos que mas faltan |

### Analista de precios y margenes (`ANALISTA`)

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/v1/pricing/policy` | Politica de margen y descuento por categoria |
| PUT | `/api/v1/pricing/policy/{categoryId}` | Fija margen minimo y umbral de descuento |
| GET | `/api/v1/pricing/margins` | Margen por producto (`categoryId`, `onlyAlerts`, `includeInactive`) |
| GET | `/api/v1/pricing/products/{id}/margin` | Margen de un producto |
| PUT | `/api/v1/pricing/products/{id}/cost` | **Solo ADMIN**: registra el costo de adquisicion |

El `GET /api/v1/products` publico incluye `fichaStatus` y `missingAttributes`, pero
**nunca** el costo: ese dato solo lo devuelven las rutas de precios.

## Roles y autorizacion

Los roles viajan en el claim `roles` del token que emite el microservicio de
usuarios. Este servicio reconoce cinco: `ADMIN`, `ASESOR`, `TECNICO`, `CURADOR`
y `ANALISTA`. El rol `CLIENTE` no necesita matchers propios porque solo consume
rutas publicas y el modulo de pedidos.

| Area | Quien escribe | Quien consulta |
|---|---|---|
| Productos, categorias y existencias | ADMIN (existencias tambien ASESOR y TECNICO) | Publico |
| Atributos tecnicos y calidad del catalogo | CURADOR, ADMIN | CURADOR, ADMIN |
| Politica de margen y consulta de margen | ANALISTA, ADMIN | ANALISTA, ADMIN |
| Costo de adquisicion | ADMIN | ANALISTA, ADMIN |

La separacion es deliberada: quien define la politica de margen no captura el
costo que esa politica controla, y quien cura el dato tecnico no puede tocar
precio ni existencia. Todo se valida en el servidor, no en la pantalla.

## Colecciones

- `products` — producto con SKU unico, precio, costo, existencia, minimo, atributos
  tecnicos, fuentes por atributo y marca de verificacion de la ficha
- `categories` — clasificacion comercial, con slug unico y politica de margen
- `stockMovements` — historial de cambios de existencia, con actor y motivo

## Reglas de negocio implementadas

1. El SKU se normaliza a mayusculas y no se puede repetir. El slug de la
   categoria tambien se normaliza: minusculas y espacios convertidos en guiones.
2. El precio y la existencia no pueden ser negativos.
3. No se registra un producto en una categoria inexistente o inactiva.
4. Los productos **no se eliminan**: se desactivan, porque los pedidos
   anteriores conservan su referencia.
5. Una categoria con productos asociados no se puede desactivar.
6. Una salida de inventario no puede dejar la existencia en negativo.
7. Todo cambio de existencia deja un movimiento registrado con su responsable.
8. Cada tipo de componente exige unos atributos obligatorios (socket y consumo en
   un procesador, socket y tipos de memoria en una placa, tipo en una memoria,
   capacidad en una fuente, consumo en una grafica, formato en un gabinete). Si
   falta alguno, la ficha queda **incompleta** y el validador no puede concluir.
9. Una ficha no se declara completa si le falta un obligatorio **o si sus
   obligatorios no tienen fuente registrada**. La fuente y la fecha quedan
   guardadas por atributo, con el usuario que verifico.
10. El margen se calcula sobre el precio de venta: `(precio - costo) / precio`.
    Si la categoria no tiene politica propia se aplica el margen global.
11. Un producto sin costo no se puede evaluar por margen: se informa `SIN_COSTO`
    en lugar de suponer un costo. El costo lo registra el administrador.

## El endpoint de existencias y el personal de la tienda

Aunque el mantenimiento del producto es exclusivo del administrador, el
movimiento de existencias **tambien** lo pueden registrar el asesor y el
tecnico: el consumo de componentes ocurre cuando el pedido entra a la linea de
ensamblaje, y esa transicion la ejecutan ellos. Por eso el `SecurityConfig`
concede `POST /api/v1/products/{id}/stock` a los tres perfiles y deja el resto
de las escrituras en manos del administrador.

## Catalogo de demostracion

Los datos de ejemplo se cargan solo si la base esta vacia (`pcforge.seed.enabled`)
y estan elegidos para poder provocar los tres veredictos del validador:

- **9 categorias** y **34 productos**.
- Casos armados a proposito: `CPU-OEM-SIN-FICHA` no declara socket,
  `MB-OEM-SIN-RAM` no declara los tipos de memoria que admite y
  `GPU-OEM-SIN-CONSUMO` no declara consumo, de modo que la regla correspondiente
  queda no concluyente.
- `RAM-DDR3-8-OEM` y `PSU-450W-OEM` existen para provocar resultados
  incompatibles (memoria no admitida y potencia insuficiente).

Las categorias `Almacenamiento`, `Gabinetes`, `Refrigeracion` y `Accesorios`
cubren tipos de componente que no participan en las reglas salvo por su consumo.

## Pruebas

```bash
mvn clean test
```

Dieciocho pruebas unitarias sobre las reglas de negocio, sin necesidad de una
base de datos en ejecucion: catalogo, existencias, calidad del dato tecnico y
margen.

## Datos de demostracion de los roles nuevos

`PricingSeeder` completa una sola vez, y de forma idempotente, el costo de los
productos que no lo tengan y la politica de margen de las categorias que no la
declaren. Esta pensado para que la demostracion funcione igual sobre una base
nueva que sobre una que ya tenia el catalogo cargado:

- El costo se calcula como el 82 % del precio, salvo cuatro referencias elegidas
  para provocar alertas (`GPU-RTX4090-24G`, `CPU-I9-14900K`, `SSD-990PRO-1TB` y
  `RAM-DDR3-8-OEM`, con margenes entre 3 % y 6 %).
- La politica es del 18 % de margen y 5 % de descuento, con variaciones en
  tarjetas graficas (22 %), memorias (15 %) y accesorios (25 %).
- Las fichas incompletas siguen siendo las tres del catalogo semilla
  (`CPU-OEM-SIN-FICHA`, `MB-OEM-SIN-RAM` y `GPU-OEM-SIN-CONSUMO`).
