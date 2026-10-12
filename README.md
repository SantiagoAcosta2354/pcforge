# PCForge

Tienda de componentes de computadora con **armado y validacion de compatibilidad**.
El sistema esta construido como cinco microservicios independientes: cuatro proveen
datos y reglas por API REST, y el quinto es la capa web que el usuario ve.

- Java 21 · Spring Boot 4.1.1 · Spring Data MongoDB · Spring Security (JWT HS256)
- Thymeleaf + Bootstrap 5 para la interfaz
- Patron **database-per-service**: cada microservicio es dueño de su propia base

```
                    Navegador
                        |
                        v
              pcforge-web  (8080)   Thymeleaf + Bootstrap, sin base de datos
                        |
        +---------------+---------------+---------------+
        v               v               v               v
   usuarios        catalogo      compatibilidad      pedidos
    (8081)          (8082)          (8083)           (8084)
        |               |               |               |
   pcforge_        pcforge_        pcforge_        pcforge_
   usuarios        catalogo      compatibilidad     pedidos
```

## Modulos

| Modulo | Puerto | Responsabilidad |
|---|---|---|
| `pcforge-usuarios` | 8081 | Cuentas, autenticacion y emision del JWT. Es el unico que firma tokens |
| `pcforge-catalogo` | 8082 | Productos, categorias, precios, existencias y movimientos |
| `pcforge-compatibilidad` | 8083 | Configurador de equipos y motor de reglas |
| `pcforge-pedidos` | 8084 | Cotizaciones, pedidos y seguimiento del ensamblaje |
| `pcforge-web` | 8080 | Capa de presentacion. No es dueña de ningun dato de negocio |

Cada modulo es un proyecto Maven independiente y tiene su propio README con el
detalle de endpoints, variables y reglas.

## Como funciona la sesion

La capa web **no valida el login**: envia las credenciales al servicio de usuarios
y, si son correctas, guarda el JWT en la sesion de Spring Security. En cada llamada
a otro servicio reenvia ese mismo token, de modo que cada microservicio decide con
la identidad real del usuario en lugar de con credenciales de administrador. Por eso
la capa web no tiene base de datos de usuarios ni guarda contrasenas.

## Reglas de compatibilidad

| Regla | Que compara |
|---|---|
| `SOCKET` | Socket del procesador frente al socket de la placa base |
| `RAM_TYPE` | Tipo de cada modulo de memoria frente a los que admite la placa |
| `POWER` | Consumo declarado del conjunto mas un margen configurable, frente a la capacidad de la fuente |

Cuando falta un dato tecnico el resultado es `NO_CONCLUYENTE` y se indica que
atributo falta: el sistema nunca afirma una compatibilidad que no puede sostener.
En el resultado global una incompatibilidad pesa mas que una duda, y una duda mas
que un OK.

## Requisitos

- JDK 21
- No hace falta Maven instalado: cada modulo trae el wrapper (`mvnw`)
- Una base MongoDB (local o Atlas)

## Ejecucion local

Los cinco servicios necesitan **el mismo secreto JWT** y cada uno su propia base.
La forma mas rapida es crear `config/application.properties` dentro de cada modulo
(esa carpeta esta ignorada por git, asi que las credenciales no se versionan):

```properties
spring.mongodb.uri=mongodb+srv://usuario:clave@cluster/pcforge_catalogo?appName=PCForge
pcforge.security.jwt.secret=una-clave-de-al-menos-32-caracteres
```

Arrancar en el orden que se quiera, desde el directorio de cada modulo:

```bash
./mvnw spring-boot:run
```

El servicio de usuarios crea las cuentas de demostracion la primera vez, solo si
la base esta vacia:

| Correo | Rol | Contrasena |
|---|---|---|
| `admin@pcforge.co` | ADMIN | La define quien despliega: define `PCFORGE_ADMIN_PASSWORD` antes del primer arranque |
| `asesor@pcforge.co` | ASESOR | `Demo1234*` |
| `tecnico@pcforge.co` | TECNICO | `Demo1234*` |
| `cliente@pcforge.co` | CLIENTE | `Demo1234*` |

La cuenta de administrador no lleva contrasena por defecto publicada: al desplegar
en un entorno accesible desde internet hay que fijar `PCFORGE_ADMIN_PASSWORD` o,
si la base ya existe, cambiarla desde la propia aplicacion
(`PATCH /api/v1/users/me/password`).

Con los cinco arriba, la aplicacion esta en **http://localhost:8080**.
Cada backend publica su API en `/swagger-ui.html` y su estado en `/actuator/health`.

En Windows hay tres scripts de apoyo en la raiz del repositorio:

| Script | Para que sirve |
|---|---|
| `arrancar-pcforge.ps1` | Levanta los cinco servicios y espera a que respondan |
| `cargar-catalogo.ps1` | Carga el catalogo de demostracion por API (idempotente) |
| `verificar-compatibilidad.ps1` | Comprueba el motor de reglas con casos conocidos |

## Variables de entorno

Todas tienen valor por defecto de desarrollo, asi que en produccion **hay que
definirlas**. El puerto se toma de `PORT`, que en Render la plataforma inyecta sola.

| Servicio | Variables |
|---|---|
| `pcforge-usuarios` | `PCFORGE_MONGO_URI`, `PCFORGE_JWT_SECRET`, `PCFORGE_JWT_EXPIRATION_MINUTES`, `PCFORGE_ADMIN_EMAIL`, `PCFORGE_ADMIN_PASSWORD`, `PCFORGE_DEMO_PASSWORD`, `PCFORGE_SEED_ENABLED` |
| `pcforge-catalogo` | `PCFORGE_MONGO_URI`, `PCFORGE_JWT_SECRET`, `PCFORGE_SEED_ENABLED` |
| `pcforge-compatibilidad` | `PCFORGE_MONGO_URI`, `PCFORGE_JWT_SECRET`, `PCFORGE_CATALOGO_URL`, `PCFORGE_CATALOGO_TIMEOUT_MS`, `PCFORGE_POWER_MARGIN`, `PCFORGE_SEED_ENABLED` |
| `pcforge-pedidos` | `PCFORGE_MONGO_URI`, `PCFORGE_JWT_SECRET`, `PCFORGE_CATALOGO_URL`, `PCFORGE_COMPATIBILIDAD_URL`, `PCFORGE_ORDER_PREFIX`, `PCFORGE_DOWNSTREAM_TIMEOUT_MS` |
| `pcforge-web` | `PCFORGE_USUARIOS_URL`, `PCFORGE_CATALOGO_URL`, `PCFORGE_COMPATIBILIDAD_URL`, `PCFORGE_PEDIDOS_URL`, `PCFORGE_DOWNSTREAM_TIMEOUT_MS`, `PCFORGE_THYMELEAF_CACHE` |

`PCFORGE_JWT_SECRET` debe ser **identico en los cuatro backends**: la firma es
simetrica y cualquiera que tenga el secreto puede validar el token sin consultar
al servicio de usuarios. La capa web no lo necesita, solo reenvia el token.

## Pruebas

```bash
./mvnw clean test
```

49 pruebas unitarias en total, sin necesidad de una base de datos en ejecucion:

| Modulo | Pruebas |
|---|---|
| `pcforge-usuarios` | 8 |
| `pcforge-catalogo` | 8 |
| `pcforge-compatibilidad` | 11 |
| `pcforge-pedidos` | 12 |
| `pcforge-web` | 10 |

## Despliegue en Render

Son **cinco servicios web** independientes dentro de un mismo proyecto de Render.

1. Crear el proyecto y conectar este repositorio de GitHub.
2. Crear los cuatro microservicios **primero** (usuarios, catalogo, compatibilidad,
   pedidos), con esta configuracion:

   | Ajuste | Valor |
   |---|---|
   | Runtime | Java |
   | Root Directory | `pcforge-<modulo>` |
   | Build Command | `./mvnw -B clean package -DskipTests` |
   | Start Command | `java -jar target/pcforge-<modulo>-0.0.1-SNAPSHOT.jar` |
   | Health Check Path | `/actuator/health` |
   | Instance Type | Free |

3. Anotar la URL publica que Render asigna a cada uno y usarla en las variables de
   entorno de los servicios que dependen de ellos.
4. Crear por ultimo `pcforge-web` con la misma configuracion y apuntando las cuatro
   variables `PCFORGE_*_URL` a las URLs del paso 3.

Dos detalles que suelen fallar:

- **Atlas debe aceptar la conexion**: el acceso de red tiene que permitir las IP
  de Render (`0.0.0.0/0` para un entorno de demostracion).
- **El plan gratuito suspende los servicios inactivos**, asi que la primera peticion
  puede tardar. Por eso `PCFORGE_DOWNSTREAM_TIMEOUT_MS` viene con un margen amplio
  y la capa web muestra una pantalla explicativa en lugar de un error tecnico.

## Sobre las credenciales

En el repositorio **no hay ninguna credencial**. La cadena de conexion a MongoDB y
el secreto JWT viven en `config/application.properties` dentro de cada modulo, que
esta excluido por `.gitignore`, y en produccion se inyectan como variables de
entorno de la plataforma.
