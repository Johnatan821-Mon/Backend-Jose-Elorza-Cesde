# Cómo funciona este backend

## ¿Qué es?
API REST construida con **Spring Boot** para gestionar clientes, citas, pagos y servicios. Usa **JWT** para autenticación y **PostgreSQL** como base de datos (H2 en desarrollo).

---

## Módulos

| Módulo | Qué hace |
|--------|----------|
| `auth` | Registro, login y generación de JWT |
| `usuario` | Ver y editar perfil, desactivar cuentas |
| `admin` | Dashboard y reportes (solo lectura, agrega datos de otros módulos) |
| `servicio` | CRUD de servicios ofrecidos |
| `agendamiento` | Crear y gestionar citas |
| `pago` | Registrar y consultar pagos |
| `redessociales` | Redes sociales asociadas a un usuario |
| `googlecalendar` | Integración opcional con Google Calendar |

---

## Flujo de una petición

```
Cliente
  │
  ▼
JwtAuthFilter          ← Lee el token del header Authorization
  │
  ▼
Controller             ← Recibe la petición HTTP
  │
  ▼
Service                ← Lógica de negocio
  │
  ▼
Repository             ← Acceso a la base de datos (Spring Data JPA)
  │
  ▼
Base de datos (PostgreSQL / H2)
```

---

## Autenticación

1. El usuario llama a `POST /api/auth/register` o `POST /api/auth/login`.
2. Recibe un **token JWT** en la respuesta.
3. En cada petición siguiente incluye el token en el header:
   ```
   Authorization: Bearer <token>
   ```
4. El filtro `JwtAuthFilter` valida el token antes de que llegue al controller.

---

## Roles

| Rol | Acceso |
|-----|--------|
| `ROLE_USER` | Sus propias citas, pagos y perfil |
| `ROLE_ADMIN` | Todo lo anterior + dashboard, reportes y gestión de usuarios |

El control por rol se aplica con `@PreAuthorize` en cada endpoint.

---

## Estructura de carpetas por módulo

Cada módulo (excepto `admin` y `usuario`) sigue la misma estructura:

```
modulo/
├── controller/   ← Endpoints HTTP
├── service/      ← Lógica de negocio
├── repository/   ← Consultas a la BD
├── model/        ← Entidad JPA (tabla en BD)
└── dto/          ← Objetos de entrada/salida de la API
```

`admin` y `usuario` no tienen `model` propio porque reutilizan la entidad `User` del módulo `auth`.

---

## Tecnologías

- **Java 17** + **Spring Boot 3**
- **Spring Security** + **JWT**
- **Spring Data JPA** + **Flyway** (migraciones de BD)
- **Lombok** (reduce código repetitivo)
- **Docker** (para levantar la BD en desarrollo)