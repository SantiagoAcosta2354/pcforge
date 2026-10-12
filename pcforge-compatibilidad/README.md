# pcforge-compatibilidad

Microservicio de **configurador de equipos y validacion de compatibilidad** de
PCForge. Es la parte diferencial del proyecto: arma una configuracion de
componentes y aplica reglas explicitas sobre ella.

- Java 21 · Spring Boot 4.1.1 · Spring Data MongoDB · Spring Security (JWT)
- Base de datos propia: `pcforge_compatibilidad`
- Consume el catalogo por API con `RestClient` (nunca lee su base de datos)

## Que significa "validar compatibilidad" aqui

No se reproducen las listas de compatibilidad de cada fabricante ni se
certifica un armado. Se aplican tres reglas explicitas:

| Regla | Que compara |
|---|---|
| SOCKET | Socket del procesador frente al socket de la placa base |
| RAM_TYPE | Tipo de cada modulo de memoria frente a los que admite la placa |
| POWER | Consumo declarado del conjunto, mas un margen configurable, frente a la capacidad de la fuente |

**Cuando faltan datos tecnicos el resultado es `NO_CONCLUYENTE`** y se indica
el atributo que falta. El sistema nunca afirma una compatibilidad que no puede
sostener. Si el catalogo no responde, la operacion falla con HTTP 503 y tampoco
se emite un veredicto.

Resultado global: una incompatibilidad pesa mas que una duda, y una duda pesa
mas que un OK.

## Variables de entorno

| Variable | Para que sirve | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP | `8083` |
| `PCFORGE_MONGO_URI` | Cadena de conexion a MongoDB | `mongodb://localhost:27017/pcforge_compatibilidad` |
| `PCFORGE_JWT_SECRET` | Secreto compartido para validar el JWT | valor de desarrollo |
| `PCFORGE_CATALOGO_URL` | Direccion base del microservicio de catalogo | `http://localhost:8082` |
| `PCFORGE_CATALOGO_TIMEOUT_MS` | Tiempo de espera al consultar el catalogo | `5000` |
| `PCFORGE_POWER_MARGIN` | Margen de potencia inicial de la regla | `20` |
| `PCFORGE_SEED_ENABLED` | Crea las reglas por defecto si la base esta vacia | `true` |

## Endpoints

| Metodo | Ruta | Acceso | Descripcion |
|---|---|---|---|
| POST | `/api/v1/configurations` | Autenticado | Crea una configuracion |
| GET | `/api/v1/configurations` | Autenticado | Lista las propias (el personal ve todas) |
| GET | `/api/v1/configurations/{id}` | Dueño o personal | Consulta una configuracion |
| DELETE | `/api/v1/configurations/{id}` | Dueño o personal | Elimina una configuracion |
| POST | `/api/v1/configurations/{id}/items` | Dueño o personal | Agrega un componente |
| DELETE | `/api/v1/configurations/{id}/items/{itemId}` | Dueño o personal | Retira un componente |
| POST | `/api/v1/configurations/{id}/validate` | Dueño o personal | Ejecuta las reglas |
| GET | `/api/v1/configurations/{id}/validations` | Dueño o personal | Historial de validaciones |
| GET | `/api/v1/rules` | Autenticado | Lista las reglas y sus parametros |
| PATCH | `/api/v1/rules/{id}` | ADMIN | Ajusta estado o parametros de una regla |

## Colecciones

- `compatibilityRules` — definicion y parametros de cada regla
- `configurations` — configuracion armada, con copia de los atributos tecnicos
- `validationResults` — evidencia de cada validacion ejecutada

## Decisiones de diseno

1. **Los parametros de las reglas viven en la base de datos**, no en el codigo:
   el margen de potencia es una decision de la tienda y se puede ajustar por
   API sin recompilar.
2. **Cada componente guarda una copia de sus atributos al agregarse**, de modo
   que una validacion anterior sigue siendo explicable aunque el catalogo cambie.
3. **La propiedad se verifica en el servidor**: un cliente no puede ver ni
   modificar la configuracion de otro, y la respuesta es 403 y no 404.
4. **Un componente inactivo del catalogo no se puede incorporar** a una
   configuracion nueva.
5. **Agregar un componente que ya estaba suma la cantidad** en lugar de crear
   una fila duplicada.

## Pruebas

```bash
mvn clean test
```

Once pruebas del motor de reglas: socket compatible, incompatible y no
concluyente; memoria incompatible; potencia suficiente, insuficiente y no
concluyente; ausencia de fuente; control de propiedad; y rechazo de productos
inactivos.
