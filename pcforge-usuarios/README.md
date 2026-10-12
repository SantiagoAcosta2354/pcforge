# pcforge-usuarios

Microservicio de **usuarios, autenticacion y emision de tokens** de PCForge.
Es el unico servicio que firma los JWT; los demas solo los validan.

- Java 21 · Spring Boot 4.1.1 · Spring Data MongoDB · Spring Security (JWT HS256)
- Base de datos propia: `pcforge_usuarios` (patron database-per-service)
- Documentacion de la API: `/swagger-ui.html`

## Puertos y variables de entorno

| Variable | Para que sirve | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP del servicio | `8081` |
| `PCFORGE_MONGO_URI` | Cadena de conexion a MongoDB | `mongodb://localhost:27017/pcforge_usuarios` |
| `PCFORGE_JWT_SECRET` | Secreto compartido para firmar y validar (**minimo 32 caracteres**) | valor de desarrollo |
| `PCFORGE_JWT_EXPIRATION_MINUTES` | Vigencia del token | `480` |
| `PCFORGE_SEED_ENABLED` | Crea las cuentas de demostracion si la base esta vacia | `true` |
| `PCFORGE_ADMIN_EMAIL` / `PCFORGE_ADMIN_PASSWORD` | Cuenta administradora inicial | `admin@pcforge.co` / `Admin1234*` |
| `PCFORGE_DEMO_PASSWORD` | Contrasena de las cuentas de demostracion | `Demo1234*` |

## Cuentas de demostracion

Se crean una sola vez, si la base no tiene usuarios:

| Correo | Rol |
|---|---|
| `admin@pcforge.co` | ADMIN |
| `asesor@pcforge.co` | ASESOR |
| `tecnico@pcforge.co` | TECNICO |
| `cliente@pcforge.co` | CLIENTE |

Las tres ultimas comparten la contrasena de demostracion.

## Endpoints

| Metodo | Ruta | Acceso | Descripcion |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Publico | Crea una cuenta con rol CLIENTE |
| POST | `/api/v1/auth/login` | Publico | Devuelve el token JWT |
| GET | `/api/v1/users/me` | Autenticado | Datos de la cuenta propia |
| PATCH | `/api/v1/users/me/password` | Autenticado | Cambio de la propia contrasena |
| GET | `/api/v1/users` | ADMIN | Lista de cuentas |
| GET | `/api/v1/users/{id}` | ADMIN | Detalle de una cuenta |
| PATCH | `/api/v1/users/{id}/roles` | ADMIN | Asigna roles |
| PATCH | `/api/v1/users/{id}/status?active=` | ADMIN | Activa o desactiva |
| GET | `/api/v1/users/technicians` | ADMIN / ASESOR | Lista los tecnicos de ensamblaje, para asignarles un pedido |

## Como funciona el token

Al iniciar sesion se devuelve un JWT firmado con HS256 que contiene:

```
sub    -> identificador del usuario
email  -> correo
name   -> nombre completo
roles  -> ["ADMIN"] | ["ASESOR"] | ["TECNICO"] | ["CLIENTE"]
iss    -> pcforge-usuarios
exp    -> fecha de expiracion
```

Como la firma es simetrica, cualquier servicio con el mismo secreto puede
verificar el token sin consultar a este microservicio. Esa es la razon por la
que el secreto debe ser identico en los cinco modulos y **no** debe quedar
escrito en el repositorio.

## Colecciones

- `users` — cuenta con nombre, correo unico, hash de contrasena, roles y estado

## Reglas de seguridad implementadas

1. Las contrasenas se guardan unicamente como hash BCrypt.
2. El registro publico **nunca** permite elegir rol: siempre asigna CLIENTE.
3. Un correo inexistente y una contrasena incorrecta devuelven el mismo
   mensaje, para no revelar que cuentas estan registradas.
4. Una cuenta inactiva no puede iniciar sesion aunque la clave sea correcta.
5. El cambio de contrasena exige la contrasena actual.
6. El correo es unico a nivel de base de datos mediante indice unico.

## Pruebas

```bash
mvn clean test
```

Ocho pruebas unitarias: seis sobre las reglas de usuarios y dos sobre la
emision y lectura del token.
