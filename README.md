# platon-microservice-account

Microservicio de gestión de cuentas de usuario y roles.

## Requisitos

- Java 25 y Maven 3.9+ (para ejecución local) o Docker con Compose.
- Proyecto `DATABASE` levantado (PostgreSQL y RabbitMQ).
- `platon-config-server` en ejecución: si no está disponible, el microservicio no inicia.
- `platon-eureka-server` en ejecución, salvo que definas `EUREKA_ENABLED=false`.

## Configuración

Toda la configuración (puerto, conexiones, Eureka) se carga desde `platon-config-server/config-repo/platon-microservice-account.yml`. El `application.yml` local solo define el nombre del servicio y la URL del Config Server (`CONFIG_SERVER_URL`, por defecto `http://localhost:8888`).

Los valores se pueden sobrescribir con variables de entorno (`SERVER_PORT`, `EUREKA_SERVER_URL`, `EUREKA_ENABLED`, etc.).

## Ejecutar

```bash
# Local
mvn spring-boot:run

# Docker
docker compose up --build
```

El servicio queda en <http://localhost:8083>.

## Swagger

Con el servicio en ejecución, abre:

- Interfaz: <http://localhost:8083/swagger-ui.html>
- Especificación OpenAPI (JSON): <http://localhost:8083/v3/api-docs>

La validación (JWT) la hace el `platon-api-gateway`.

## Notas de la API

Los endpoints están documentados en Swagger. Estas son las reglas que conviene conocer:

### Roles

Los roles son `ADMIN`, `USER` y `SELLER`. Consulta `GET /api/roles` para obtener los `idRole` reales y envíalo en `POST` y `PUT`; un rol ausente o inexistente devuelve HTTP 400. Los permisos de cada rol se aplican en el gateway.

### Listado de cuentas

`GET /api/accounts` y `GET /api/accounts/staff` devuelven una página (`content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`) ordenada por `idUser`. Cada usuario incluye, además de sus datos:

- `paidOrderCount`: cantidad de pedidos en estado `PAGADO`.
- `totalPurchases`: suma de los importes de esos pedidos.

Los filtros se aplican antes de paginar. Parámetros opcionales:

| Parámetro | Descripción | Por defecto |
| --- | --- | --- |
| `page` | Página, desde 0 | `0` |
| `size` | Usuarios por página (1 a 100) | `20` |
| `active` | `true` activos, `false` inactivos; omitir para ambos | — |
| `minPaidOrders` | Mínimo de pedidos pagados | `0` |

```bash
curl "http://localhost:8083/api/accounts?page=0&size=10&active=true&minPaidOrders=5"
```

## Orden recomendado

1. `platon-eureka-server`
2. `platon-config-server`
3. Microservicios
4. `platon-api-gateway`