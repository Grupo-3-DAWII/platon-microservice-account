# microservice-account

Microservicio backend para gestion de cuentas. Esta configurado con Java, Spring Boot, Spring Data JDBC, Spring MVC, Spring Security, Lombok, MapStruct y PostgreSQL.

## Requisitos

- Java 21 o superior
- Maven 3.9 o superior
- Docker Desktop, opcional para levantar PostgreSQL local

## Base de datos local

Ejecuta PostgreSQL localmente con Docker o con una instalacion local. El script SQL de referencia se encuentra en `../../ai-context/sql.txt` desde la raiz del microservicio.

Las tablas `status` y `account` deben existir antes de iniciar la aplicacion.

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

El reinicio de la base depende del mecanismo usado para ejecutar PostgreSQL. Si usas Docker Compose, elimina el volumen correspondiente y vuelve a ejecutar el script SQL.

## Ejecutar

```bash
mvn spring-boot:run
```

## Pruebas y cobertura

```bash
mvn verify
```

El build genera `target/site/jacoco/index.html` y falla si la cobertura de la logica propia baja del 100%.

## Ejecutar con Docker Compose

El archivo `.env` contiene las variables locales de desarrollo. Para iniciar PostgreSQL y el microservicio:

```bash
docker compose up --build
```

La API queda disponible en `http://localhost:8080`. El SQL de `ai-context` se ejecuta automáticamente solo cuando se crea el volumen por primera vez.

Para reiniciar la base desde cero:

```bash
docker compose down -v
docker compose up --build
```

Si necesitas cambiar puertos o credenciales, modifica `.env`. No subas ese archivo al repositorio; usa `.env.example` como referencia.

Credenciales HTTP Basic para pruebas locales:

- admin: `admin` / `admin123`
- lectura: `student` / `student123`

Las credenciales son temporales y estan definidas en memoria para desarrollo. No deben usarse en produccion.

## Endpoints

- `GET /api/accounts`
- `GET /api/accounts/{id}`
- `GET /api/accounts/validate/{username}`
- `POST /api/accounts`
- `PUT /api/accounts/{id}`
- `DELETE /api/accounts/{id}`
- `GET /api/statuses`
- `GET /api/statuses/{id}`
- `POST /api/statuses`
- `PUT /api/statuses/{id}`
- `DELETE /api/statuses/{id}`

La consulta de cuentas admite paginacion y filtros:

```text
GET /api/accounts?page=0&size=20&search=ana&idStatus=1
```

`size` acepta valores entre 1 y 100. La respuesta incluye `content`, `page`, `size`, `totalElements`, `totalPages`, `first` y `last`.

## OpenAPI

Con la aplicacion levantada:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificacion JSON: `http://localhost:8080/v3/api-docs`

Los errores usan `ProblemDetail` con `code`, `path` e `instance`. Los errores de validacion incluyen `fieldErrors`.

Ejemplo:

```bash
curl -u admin:admin123 http://localhost:8080/api/accounts
```

Ejemplo de registro:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/accounts ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Ana\",\"lastname\":\"Torres\",\"username\":\"atorres\",\"email\":\"ana@demo.com\",\"nro\":\"999888777\",\"direccion\":\"Av. Lima 123\",\"idStatus\":1}"
```
