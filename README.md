# microservice-account

Microservicio backend para gestion de cuentas. Esta configurado con Java, Spring Boot, Spring Data JPA, Spring MVC, Spring Security, Lombok y PostgreSQL.

## Requisitos

- Java 25
- Maven 3.9 o superior
- Docker Desktop, opcional para levantar PostgreSQL local
- `platon-config-server` en el puerto 8888
- `platon-eureka-server` en el puerto 8761, salvo que se defina `EUREKA_ENABLED=false`

## Base de datos local

```bash
cd C:\Users\nekro\Desktop\CIBERTEC\DATABASE
docker compose up -d
```

El compose monta `C:\Users\nekro\Desktop\CIBERTEC\DATABASE\init` para crear las tablas iniciales de PostgreSQL.

La configuracion por defecto usa:

- host: `localhost`
- puerto: `5432`
- database: `account_db`
- usuario: `account_user`
- password: `account_pass`

Tablas iniciales:

- `status`: `idstatus`, `name`
- `account`: `iduser`, `name`, `lastname`, `username`, `idstatus`, `email`, `nro`, `direccion`

Estados iniciales:

- `active`
- `inactive`

Si el volumen `account_postgres_data` ya existia antes de agregar el script SQL, PostgreSQL no ejecutara nuevamente los archivos de inicializacion. Para reiniciar la base local desde cero:

```bash
cd C:\Users\nekro\Desktop\CIBERTEC\DATABASE
docker compose down -v
docker compose up -d
```

## Ejecutar

```bash
mvn spring-boot:run
```

El microservicio obtiene su configuracion desde Config Server y arranca en
`http://localhost:8083`. `src/main/resources/application.yml` conserva solo
el nombre de aplicacion y la importacion de configuracion remota.
El puerto, datasource e inicializacion SQL estan en
`../platon-config-server/config-repo/platon-microservice-account.yml`;
la URL de Eureka se hereda de `config-repo/application.yml`.
Config Server debe arrancar desde su carpeta raiz para encontrar `./config-repo`.
Si no esta disponible, el microservicio falla al cargar configuracion remota.

Orden de arranque:

1. PostgreSQL con los esquemas inicializados.
2. `platon-eureka-server`.
3. `platon-config-server`.
4. `platon-microservice-account` y los demas microservicios.
5. `platon-api-gateway`.

Se pueden ajustar `CONFIG_SERVER_URL`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`SERVER_PORT` y `EUREKA_SERVER_URL` mediante variables de entorno.
Para ejecutar sin registro en Eureka, definir `EUREKA_ENABLED=false`.

Credenciales HTTP Basic para pruebas:

- admin: `admin` / `admin123`
- usuario: `usuario` / `usuario123`

## Endpoints

- `GET /api/accounts`
- `GET /api/accounts/staff` (solo cuentas con rol `ADMIN` o `SELLER`)
- `GET /api/accounts/{id}`
- `GET /api/accounts/validate/{username}`
- `POST /api/accounts`
- `PUT /api/accounts/{id}`
- `DELETE /api/accounts/{id}`
- `GET /api/roles`

### Roles

El catalogo admite `USER` (usuario), `SELLER` (vendedor) y `ADMIN` (administrador).
Consultar `GET /api/roles` para obtener los `idRole` reales; no asumir identificadores fijos.
Enviar `idRole` en `POST /api/accounts` y `PUT /api/accounts/{id}`.
Un rol ausente o inexistente devuelve HTTP 400.
Las respuestas de cuentas incluyen `idRole` y `role` (nombre del rol).
Tambien incluyen `createdAt` en formato ISO 8601 (fecha y hora local del servidor).
Se asigna al crear la cuenta y no cambia al editarla. Las cuentas anteriores a la
incorporacion de este campo devuelven `null` si no se conoce su fecha original.
El init de Docker incluye la columna; para una base existente, ejecutar
`../DATABASE/init/03-add-user-created-at.sql` una vez (es idempotente).
Estos roles se guardan en la cuenta; los permisos de cada rol requieren reglas de autorizacion en el gateway.

El catalogo se inicializa en `../DATABASE/init/01-create-database-schema.sql`,
montado por Docker en `/docker-entrypoint-initdb.d`. Incluye `ADMIN`, `USER` y `SELLER`.
PostgreSQL ejecuta el init solo al crear un volumen de datos nuevo.
La base local existente ya tiene los tres roles y conserva sus identificadores actuales.

### Listado de usuarios

`GET /api/accounts` devuelve los campos `idUser`, `name`, `lastname`,
`username`, `idRole`, `role`, `email`, `nro`, `direccion`, `idStatus`, `status`
y agrega por cada usuario:

- `paidOrderCount`: cantidad de registros de `payment.products_sold` con estado `PAGADO`.
- `totalPurchases`: suma de `number_sold * products.product.sale_price` de esos pedidos pagados.

El listado ahora devuelve un objeto paginado (antes devolvia un arreglo):
`content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`.
Los filtros se aplican antes de paginar y los totales corresponden a los usuarios filtrados.
El orden es por `idUser` ascendente.

`GET /api/accounts/staff` usa el mismo formato y parametros del listado,
restringiendo los resultados a `ADMIN` y `SELLER` antes de calcular totales y paginar.

```bash
curl "http://localhost:8083/api/accounts/staff?page=0&size=10"
```

Parametros opcionales:

- `page`: pagina desde 0; por defecto 0.
- `size`: usuarios por pagina, entre 1 y 100; por defecto 20.
- `active`: `true` para activos, `false` para inactivos; omitir para ambos.
- `minPaidOrders`: minimo inclusivo de pedidos pagados; por defecto 0.
  Usar `5` para usuarios con mas de 4 compras pagadas.

```bash
curl "http://localhost:8083/api/accounts?page=0&size=10&active=true&minPaidOrders=5"
```

Los pedidos `PENDIENTE` y `CANCELADO` quedan fuera de ambos calculos.
Los usuarios sin pedidos pagados aparecen con ambos valores en cero.
Cada registro de `products_sold` cuenta como un pedido, independientemente de la cantidad de unidades.
La consulta usa los esquemas compartidos `account`, `payment` y `products`.
No requiere crear otra tabla de pedidos.

La tabla actual no guarda el precio historico de la compra: `totalPurchases`
usa el precio de venta actual del producto y cambiara si se modifica ese precio.
Para conservar el importe realmente pagado se debe guardar el precio o total al registrar cada compra.

Ejemplo:

```bash
curl -u admin:admin123 http://localhost:8083/api/accounts
```

Ejemplo de registro:

```bash
curl -u admin:admin123 -X POST http://localhost:8083/api/accounts ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Ana\",\"lastname\":\"Torres\",\"username\":\"atorres\",\"password\":\"ana123\",\"email\":\"ana@demo.com\",\"nro\":\"999888777\",\"direccion\":\"Av. Lima 123\",\"idStatus\":1}"
```
