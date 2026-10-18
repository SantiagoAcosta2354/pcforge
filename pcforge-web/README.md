# pcforge-web

Capa de presentacion de PCForge, construida con **Thymeleaf** y **Bootstrap 5**.
Es la cara visible del sistema: la que usa el comprador para armar su equipo y
la que usa el personal de la tienda para atender los pedidos.

- Java 21 · Spring Boot 4.1.1 · Thymeleaf · Bootstrap 5.3 · Spring Security
- **Sin base de datos propia**: no es dueña de ningun dato de negocio, solo presenta
  la informacion que le entregan los cuatro microservicios.
- Se comunica por API con `usuarios` (8081), `catalogo` (8082),
  `compatibilidad` (8083) y `pedidos` (8084).

## Pantallas

| Ruta | Acceso | Que permite hacer |
|---|---|---|
| `/` | Publico | Portada con el resumen del flujo y productos destacados |
| `/catalogo` | Publico | Buscar y filtrar productos por categoria y tipo |
| `/catalogo/{id}` | Publico | Ficha tecnica del componente y agregarlo al equipo |
| `/registro` | Publico | Crear una cuenta (siempre con perfil de cliente) |
| `/login` | Publico | Iniciar sesion |
| `/armar` | Autenticado | Armar el equipo, validar compatibilidad y solicitar cotizacion |
| `/pedidos` | Autenticado | Pedidos propios; el personal ve los de todos los clientes |
| `/pedidos/{id}` | Dueño o personal | Detalle, historial de estados, observaciones y cambios de estado |
| `/inventario` | ADMIN | Existencias, alertas de minimo y registro de movimientos |
| `/admin/productos` | ADMIN | Gestion del catalogo: alta, edicion y activacion de productos |
| `/admin/categorias` | ADMIN | Gestion de categorias: alta, edicion y activacion |
| `/acceso-denegado` | Autenticado | Pantalla que se muestra cuando el perfil no tiene permiso para la ruta |

## Como se maneja la sesion y el token

El inicio de sesion **no se valida aqui**: la capa web envia las credenciales al
microservicio de usuarios y, si son correctas, guarda el token JWT dentro de la
sesion de Spring Security. En cada llamada a otro servicio ese token se reenvia
tal cual.

Esa decision es deliberada. Si la capa web usara credenciales propias de
administrador, podria leer los datos de cualquier cliente y la verificacion de
propiedad que hacen los servicios quedaria anulada. Reenviando el token, cada
servicio sigue decidiendo con la identidad real del usuario.

Como consecuencia, la capa web **no tiene base de datos de usuarios ni guarda
contrasenas**: no hay nada que proteger en ese sentido.

## Variables de entorno

| Variable | Para que sirve | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP | `8080` |
| `PCFORGE_USUARIOS_URL` | Direccion del servicio de usuarios | `http://localhost:8081` |
| `PCFORGE_CATALOGO_URL` | Direccion del servicio de catalogo | `http://localhost:8082` |
| `PCFORGE_COMPATIBILIDAD_URL` | Direccion del servicio de compatibilidad | `http://localhost:8083` |
| `PCFORGE_PEDIDOS_URL` | Direccion del servicio de pedidos | `http://localhost:8084` |
| `PCFORGE_DOWNSTREAM_TIMEOUT_MS` | Tiempo de espera hacia los servicios | `60000` |
| `PCFORGE_THYMELEAF_CACHE` | Cache de plantillas de Thymeleaf | `true` |

El tiempo de espera es amplio a proposito: en el plan gratuito de la nube los
servicios se suspenden cuando llevan un rato sin uso y la primera peticion tarda
en despertarlos. Por eso la aplicacion muestra una pantalla explicativa cuando
alguno no responde, en lugar de un error tecnico.

## Seguridad aplicada

1. **Proteccion CSRF activa** en todos los formularios, con su campo oculto explicito.
2. **Autorizacion por ruta** en la configuracion de Spring Security y, ademas,
   aplicada de nuevo en cada microservicio, que es quien decide de verdad.
3. **Mensajes de error uniformes**: las reglas de negocio se muestran en lenguaje
   claro y vuelven a la pagina de origen, sin romper la navegacion.
4. **Sin trazas en pantalla**: `server.error.include-stacktrace=never`.
5. **Sin credenciales en el navegador**: el token vive en la sesion del servidor.

## Nota sobre Bootstrap

Bootstrap se carga desde un CDN (jsdelivr). Requiere conexion a internet en el
navegador. Si se necesita una demostracion sin red, basta con descargar
`bootstrap.min.css` y `bootstrap.bundle.min.js` en `src/main/resources/static/`
y cambiar los enlaces de `templates/fragments/layout.html`.

## Pruebas

```bash
mvn clean test
```

Once pruebas sobre la traduccion de roles a autoridades, las etiquetas de cada
perfil —incluidos curador de datos tecnicos y analista de precios y margenes— y
el proveedor de autenticacion (credenciales validas, incorrectas y servicio no
disponible).
