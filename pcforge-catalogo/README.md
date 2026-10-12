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

## Colecciones

- `products` — producto con SKU unico, precio, existencia, minimo y atributos tecnicos
- `categories` — clasificacion comercial, con slug unico
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
- Casos armados a proposito: `CPU-OEM-IND` no declara socket, `MB-AM5-SIN-RAM`
  no declara los tipos de memoria que admite y `GPU-OEM-IND` no declara consumo,
  de modo que la regla correspondiente queda no concluyente.
- `RAM-DDR3-8` y `PSU-450W-BAS` existen para provocar resultados incompatibles
  (memoria no admitida y potencia insuficiente).

Las categorias `Almacenamiento`, `Gabinetes`, `Refrigeracion` y `Accesorios`
cubren tipos de componente que no participan en las reglas salvo por su consumo.

## Pruebas

```bash
mvn clean test
```

Ocho pruebas unitarias sobre las reglas de negocio, sin necesidad de una base
de datos en ejecucion.
