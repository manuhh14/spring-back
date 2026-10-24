# Residencias Profesionales — API REST

API para gestionar las residencias profesionales: registro de alumnos y empresas, publicación de proyectos, postulaciones, asignación de asesores académicos y expediente de documentos.

**Stack:** Java 21 · Spring Boot 4.1 · Spring Data JPA · PostgreSQL · Spring Security con JWT · springdoc-openapi (Swagger)

## Requisitos

- JDK 21 (comprueba `JAVA_HOME`; con la 17 no compila)
- PostgreSQL con una base de datos llamada `residencias-db`

## Puesta en marcha

1. Copia `.env.example` como `.env` y rellena los valores. `.env` no se sube al repositorio.

   | Variable | Descripción |
   |---|---|
   | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a PostgreSQL |
   | `JWT_SECRET` | Clave para firmar los tokens (mínimo 32 caracteres) |
   | `ADMIN_CORREO`, `ADMIN_PASSWORD` | Administrador que se crea al arrancar si no existe ninguno |
   | `JWT_EXPIRACION_MINUTOS` | Opcional, 480 por defecto |
   | `UPLOADS_DIR` | Opcional, carpeta de los PDF (`uploads` por defecto) |
   | `CORS_ORIGENES` | Opcional, orígenes del frontend separados por coma |

2. Arranca la aplicación:

   ```bash
   ./mvnw spring-boot:run
   ```

3. Abre la documentación interactiva en <http://localhost:8080/swagger-ui.html>. Haz login en `POST /api/auth/login`, copia el `token` y pégalo en **Authorize**.

## Tests

```bash
./mvnw test
```

Los tests usan el perfil `test` con una base H2 en memoria, así que no necesitan PostgreSQL. Incluyen pruebas unitarias de las reglas de negocio y una prueba de integración que recorre el ciclo completo de una residencia.

## Roles y flujo

| Rol | Cómo se crea | Qué puede hacer |
|---|---|---|
| `ADMIN` | Al arrancar (`ADMIN_*`) o con `POST /api/usuarios` | Todo: verificar empresas, dar de alta asesores, asignar asesores |
| `ALUMNO` | `POST /api/auth/registro/alumno` | Postularse, subir documentos de su residencia |
| `EMPRESA` | `POST /api/auth/registro/empresa` | Publicar proyectos (una vez verificada), aceptar o rechazar postulaciones |
| `ASESOR` | El admin con `POST /api/asesores` | Revisar documentos y finalizar las residencias asignadas |

1. La empresa se registra y un admin la verifica (`PATCH /api/empresas/{id}/verificacion`).
2. La empresa publica un proyecto con un cupo de alumnos (`POST /api/proyectos`).
3. El alumno se postula a un proyecto `DISPONIBLE` (`POST /api/postulaciones`).
4. La empresa acepta o rechaza (`PATCH /api/postulaciones/{id}/estado`). Al aceptar:
   - el alumno pasa a `EN_PROCESO` y sus otras postulaciones pendientes se rechazan;
   - si se completa el cupo, el proyecto pasa a `ASIGNADO` y se rechazan las demás pendientes.
5. El admin asigna un asesor (`PATCH /api/postulaciones/{id}/asesor`), respetando su `maximoAlumnos`.
6. El alumno sube PDFs al expediente (`POST /api/documentos`, multipart) y el asesor los aprueba o rechaza (`PATCH /api/documentos/{id}/revision`).
7. Con el `REPORTE_FINAL` aprobado, el asesor finaliza la residencia (`PATCH /api/postulaciones/{id}/finalizar`) y el alumno queda en `FINALIZADA`.

## Endpoints

Todos requieren `Authorization: Bearer <token>` salvo `/api/auth/**` y Swagger.

| Recurso | Endpoints principales |
|---|---|
| Auth | `POST /api/auth/login`, `POST /api/auth/registro/alumno`, `POST /api/auth/registro/empresa` |
| Usuarios | `GET /api/usuarios/me`; CRUD de admin en `/api/usuarios` |
| Alumnos | `GET /api/alumnos/me`, `GET /api/alumnos/{numeroControl}`, `PUT`, `DELETE` |
| Empresas | `GET /api/empresas`, `GET /api/empresas/me`, `PUT`, `PATCH /{id}/verificacion`, `DELETE` |
| Asesores | `GET /api/asesores/me`, `GET /api/asesores/me/postulaciones`; CRUD de admin |
| Proyectos | `GET /api/proyectos?estado=DISPONIBLE`, `GET /mis-proyectos`, `GET /{id}/postulaciones`, `POST`, `PUT`, `PATCH /{id}/cerrar`, `DELETE` |
| Postulaciones | `GET /mis-postulaciones`, `GET /{id}`, `GET /{id}/documentos`, `POST`, `PATCH /{id}/estado`, `PATCH /{id}/asesor`, `PATCH /{id}/finalizar`, `DELETE` |
| Documentos | `POST /api/documentos` (multipart: `idPostulacion`, `tipoDocumento`, `archivo`), `GET /{id}/archivo`, `PATCH /{id}/revision`, `DELETE` |

Los listados paginados aceptan `?page=0&size=20&sort=campo,asc`.

## Errores

Todas las respuestas de error tienen el mismo formato:

```json
{
  "timestamp": "2026-10-23T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "La petición contiene datos inválidos",
  "ruta": "/api/auth/registro/alumno",
  "errores": { "correo": "must be a well-formed email address" }
}
```

| Código | Cuándo |
|---|---|
| 400 | Validación fallida, JSON mal formado o archivo que no es PDF |
| 401 | Sin token, token inválido o credenciales incorrectas |
| 403 | El rol no tiene permiso o el recurso no te pertenece |
| 404 | El recurso no existe |
| 409 | Se viola una regla de negocio (cupo lleno, postulación duplicada, empresa sin verificar, etc.) |
