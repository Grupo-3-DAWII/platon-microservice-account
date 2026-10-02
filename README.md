# microservice-account

Microservicio backend para gestion de cuentas. Esta configurado con Java, Spring Boot, Spring Data JPA, Spring MVC, Spring Security, Lombok y PostgreSQL.

## Requisitos

- Java 21 o superior
- Maven 3.9 o superior
- Docker Desktop, opcional para levantar PostgreSQL local

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

Credenciales HTTP Basic para pruebas:

- admin: `admin` / `admin123`
- lectura: `student` / `student123`

## Endpoints

- `GET /api/accounts`
- `GET /api/accounts/{id}`
- `GET /api/accounts/validate/{username}`
- `POST /api/accounts`
- `PUT /api/accounts/{id}`
- `DELETE /api/accounts/{id}`

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
