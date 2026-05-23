# Registro de Desarrollo — API Clientes

> Proyecto: `com.jorgelorza.clientes`  
> Stack: Spring Boot 3.5.14 · Java 21 · Maven · H2 (dev) · PostgreSQL (prod)  
> Estado: compilado y corriendo ✓

---

## Índice

1. [Estructura del proyecto](#1-estructura-del-proyecto)
2. [Configuración inicial](#2-configuración-inicial)
3. [Módulo de autenticación](#3-módulo-de-autenticación)
4. [Módulos de negocio](#4-módulos-de-negocio)
5. [Infraestructura](#5-infraestructura)
6. [Documentación del código](#6-documentación-del-código)
7. [Problemas resueltos](#7-problemas-resueltos)
8. [Notas técnicas](#8-notas-técnicas)

---

## 1. Estructura del proyecto

Arquitectura **package-by-feature**: cada módulo agrupa sus propias capas (model, dto, repository, service, controller) en lugar de agrupar por capa.

```
src/main/java/com/jorgelorza/clientes/
├── auth/
│   ├── controller/       AuthController
│   ├── dto/              LoginRequest, RegisterRequest, AuthResponse
│   ├── model/            User, Role
│   ├── repository/       UserRepository
│   ├── security/         JwtAuthFilter, JwtUtil, SecurityConfig, UserDetailsServiceImpl
│   └── service/          AuthService
├── usuario/
│   ├── controller/       UsuarioController
│   ├── dto/              UsuarioResponse, ActualizarPerfilRequest
│   └── service/          UsuarioService
├── servicio/
│   ├── controller/       ServicioController
│   ├── dto/              ServicioRequest, ServicioResponse
│   ├── model/            Servicio
│   ├── repository/       ServicioRepository
│   └── service/          ServicioService
├── agendamiento/
│   ├── controller/       CitaController
│   ├── dto/              CitaRequest, CitaResponse
│   ├── model/            Cita, EstadoCita
│   ├── repository/       CitaRepository
│   └── service/          CitaService
├── pago/
│   ├── controller/       PagoController
│   ├── dto/              PagoRequest, PagoResponse
│   ├── model/            Pago, EstadoPago, MetodoPago
│   ├── repository/       PagoRepository
│   └── service/          PagoService
├── redessociales/
│   ├── controller/       RedSocialController
│   ├── dto/              RedSocialRequest, RedSocialResponse
│   ├── model/            RedSocial, TipoRedSocial
│   ├── repository/       RedSocialRepository
│   └── service/          RedSocialService
├── googlecalendar/
│   ├── config/           GoogleCalendarConfig
│   ├── controller/       GoogleCalendarController
│   ├── dto/              EventoCalendarioRequest, EventoCalendarioResponse
│   └── service/          GoogleCalendarService
├── admin/
│   ├── controller/       AdminController
│   ├── dto/              DashboardResponse, ReporteIngresoResponse
│   └── service/          AdminService
└── common/
    ├── exception/        GlobalExceptionHandler, ResourceNotFoundException
    └── response/         ApiResponse<T>
```

---

## 2. Configuración inicial

### 2.1 Dependencias añadidas al `pom.xml`

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-web` | managed | API REST con Tomcat |
| `spring-boot-starter-security` | managed | Autenticación y autorización |
| `spring-boot-starter-data-jpa` | managed | ORM con Hibernate |
| `spring-boot-starter-validation` | managed | Validación de DTOs con `@Valid` |
| `jjwt-api / jjwt-impl / jjwt-jackson` | 0.12.6 | Generación y validación de JWT |
| `h2` | managed (runtime) | Base de datos en memoria para dev/CI |
| `postgresql` | managed (runtime) | Base de datos de producción |
| `flyway-core` | managed | Migraciones de esquema |
| `flyway-database-postgresql` | managed | Soporte Flyway para PostgreSQL |
| `lombok` | managed (optional) | Reducción de boilerplate |
| `google-api-services-calendar` | v3-rev20240111-2.0.0 | Google Calendar API |
| `google-auth-library-oauth2-http` | 1.23.0 | Autenticación OAuth2 con Google |

Lombok requiere configuración adicional en el plugin `maven-compiler-plugin`:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
</annotationProcessorPaths>
```

### 2.2 Perfiles de configuración

**`application.properties`** — configuración común a todos los perfiles:
- Perfil activo por variable de entorno: `${SPRING_PROFILES_ACTIVE:dev}`
- `spring.jpa.open-in-view=false` — evita lazy-loading fuera de transacción
- JWT: secret y expiración via variables de entorno con defaults de desarrollo
- Google Calendar desactivado por defecto: `google.calendar.enabled=false`

**`application-dev.properties`** — entorno local y CI:
- H2 en memoria con `MODE=PostgreSQL` para compatibilidad con las mismas migraciones SQL
- Flyway habilitado, Hibernate en modo `validate`
- H2 Console disponible en `/h2-console`
- `show-sql=true`

**`application-prod.properties`** — producción:
- PostgreSQL vía variables de entorno `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- Google Calendar activable con `GOOGLE_CALENDAR_ENABLED=true`
- Sin consola H2

---

## 3. Módulo de autenticación

### 3.1 Entidades

**`User`** implementa `UserDetails` de Spring Security.  
Decisión de diseño: se usa `@Getter` + `@Setter` en lugar de `@Data` para evitar conflicto con el método `getPassword()` que Spring Security exige implementar explícitamente.

- Campos: `id`, `name`, `email` (único), `password` (bcrypt), `phone`, `active`, `role`
- `getUsername()` devuelve `email` — el "username" en este sistema es siempre el email
- `active = true` por defecto con `@Builder.Default`

**`Role`** enum con valores `ROLE_USER` y `ROLE_ADMIN`.  
El prefijo `ROLE_` es requerido por Spring Security para que `hasRole('ADMIN')` funcione en `@PreAuthorize`.

### 3.2 Flujo de registro

```
POST /api/auth/register
  → AuthController.register()
  → AuthService.register()
      → verificar email duplicado (lanza IllegalArgumentException si existe)
      → codificar contraseña con BCryptPasswordEncoder
      → guardar User con ROLE_USER
      → generar JWT
  ← AuthResponse { token, email, name, role }
```

### 3.3 Flujo de login

```
POST /api/auth/login
  → AuthController.login()
  → AuthService.login()
      → authenticationManager.authenticate(UsernamePasswordAuthenticationToken)
          → delega a DaoAuthenticationProvider
          → DaoAuthenticationProvider llama UserDetailsServiceImpl.loadUserByUsername()
          → compara contraseña con BCrypt
          → lanza BadCredentialsException si falla
      → generar JWT si las credenciales son correctas
  ← AuthResponse { token, email, name, role }
```

### 3.4 Validación de JWT en cada request

El filtro `JwtAuthFilter` (extiende `OncePerRequestFilter`) intercepta cada request:

1. Extrae el header `Authorization: Bearer <token>`
2. Obtiene el email (subject) del token con `JwtUtil.extractUsername()`
3. Si no hay autenticación en el `SecurityContextHolder`, carga el usuario de BD
4. Valida que el token pertenezca al usuario y no haya expirado
5. Inyecta `UsernamePasswordAuthenticationToken` en el contexto
6. Continúa la cadena de filtros

### 3.5 Configuración de Spring Security (`SecurityConfig`)

- **CSRF deshabilitado**: API stateless con JWT, no usa cookies
- **Sesión STATELESS**: no se crea `HttpSession`
- **Rutas públicas**: `/api/auth/**` y `/h2-console/**`
- **Todo lo demás**: requiere autenticación
- **`@EnableMethodSecurity`**: habilita `@PreAuthorize` en controllers y services
- **`JwtAuthFilter`** se ejecuta antes de `UsernamePasswordAuthenticationFilter`

### 3.6 Separación de `UserDetailsServiceImpl`

Se extrajo como `@Service` independiente en lugar de definirlo como `@Bean` dentro de `SecurityConfig` para evitar la dependencia circular:

```
JwtAuthFilter → UserDetailsService → SecurityConfig → JwtAuthFilter
```

### 3.7 `JwtUtil`

- Algoritmo: HMAC-SHA256 (`signWith(getSigningKey())`)
- Clave derivada del secreto Base64 configurado en `jwt.secret`
- Token contiene: subject (email), issuedAt, expiration
- Expiración configurable via `jwt.expiration` (default 86400000 ms = 24 horas)

---

## 4. Módulos de negocio

### 4.1 Módulo Usuarios (`/api/usuarios`)

Reutiliza la entidad `User` del módulo auth — no define entidad propia.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/api/usuarios` | ADMIN | Lista todos los usuarios |
| GET | `/api/usuarios/perfil` | Autenticado | Perfil del usuario actual |
| PUT | `/api/usuarios/perfil` | Autenticado | Actualiza nombre y teléfono |
| GET | `/api/usuarios/{id}` | ADMIN | Obtiene usuario por id |
| DELETE | `/api/usuarios/{id}` | ADMIN | Soft delete (active=false) |

El email del usuario autenticado se obtiene de `Authentication.getName()`, que devuelve el subject del JWT.

### 4.2 Módulo Servicios (`/api/servicios`)

Catálogo de servicios del negocio (cortes, tratamientos, etc.).

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/api/servicios` | Autenticado | Solo servicios activos |
| GET | `/api/servicios/todos` | ADMIN | Todos (incluye desactivados) |
| GET | `/api/servicios/{id}` | Autenticado | Detalle de un servicio |
| POST | `/api/servicios` | ADMIN | Crea servicio (201 Created) |
| PUT | `/api/servicios/{id}` | ADMIN | Actualiza servicio |
| DELETE | `/api/servicios/{id}` | ADMIN | Soft delete (activo=false) |

`ServicioService.findById()` es público para uso cross-módulo desde `CitaService`.

### 4.3 Módulo Agendamiento (`/api/citas`)

Gestión de citas entre usuarios y servicios.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/citas` | Autenticado | Agenda una cita propia |
| GET | `/api/citas/mis-citas` | Autenticado | Historial de citas propias |
| DELETE | `/api/citas/{id}` | Autenticado | Cancela cita propia |
| GET | `/api/citas` | ADMIN | Todas las citas |
| GET | `/api/citas/rango?inicio=&fin=` | ADMIN | Citas en rango de fechas |
| PATCH | `/api/citas/{id}/estado?estado=` | ADMIN | Cambia estado manualmente |

**Ciclo de vida de una cita**: `PENDIENTE → CONFIRMADA → EN_CURSO → COMPLETADA / CANCELADA`

**Validación de solapamiento**: antes de crear una cita se verifica que no exista otra para el mismo servicio en la misma fecha/hora (excluyendo las CANCELADAS para reutilizar horarios liberados).

**Regla de cancelación**: el servicio valida que la cita pertenezca al usuario autenticado y que no esté ya COMPLETADA.

**Fechas de rango**: formato ISO-8601, p.ej. `2025-06-01T09:00:00`.

### 4.4 Módulo Pagos (`/api/pagos`)

Registro de pagos asociados 1-a-1 con citas.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/pagos` | ADMIN | Registra pago y completa la cita |
| GET | `/api/pagos` | ADMIN | Lista todos los pagos |
| GET | `/api/pagos/cita/{citaId}` | Autenticado | Pago de una cita específica |
| GET | `/api/pagos/usuario/{usuarioId}` | ADMIN | Pagos de un usuario |
| PATCH | `/api/pagos/{id}/reembolsar` | ADMIN | Marca pago como REEMBOLSADO |

**Reglas de negocio**:
- El monto se toma del precio del servicio (no del request) para evitar manipulaciones
- Al registrar un pago exitoso, la cita pasa automáticamente a `COMPLETADA`
- No se puede pagar una cita `CANCELADA`
- No se puede duplicar el pago de una misma cita
- Solo se puede reembolsar un pago en estado `PAGADO`

**Estados de pago**: `PENDIENTE → PAGADO → REEMBOLSADO` (también existe `FALLIDO` para integraciones de pasarela externas)

### 4.5 Módulo Redes Sociales (`/api/redes-sociales`)

Gestión de perfiles de redes sociales del negocio para mostrar en la app/sitio.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/api/redes-sociales` | Autenticado | Solo redes activas |
| GET | `/api/redes-sociales/todas` | ADMIN | Todas (incluye desactivadas) |
| POST | `/api/redes-sociales` | ADMIN | Crea perfil (201 Created) |
| PUT | `/api/redes-sociales/{id}` | ADMIN | Actualiza perfil |
| DELETE | `/api/redes-sociales/{id}` | ADMIN | Soft delete (activo=false) |

Plataformas: `INSTAGRAM`, `FACEBOOK`, `TWITTER`, `TIKTOK`, `YOUTUBE`, `LINKEDIN`, `OTRO`

### 4.6 Módulo Google Calendar (`/api/google-calendar`)

Integración opcional con Google Calendar API v3.

> **Solo disponible cuando `google.calendar.enabled=true`.**  
> En dev y CI el módulo completo no se carga — no se necesita `credentials.json`.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/google-calendar/eventos` | ADMIN | Crea evento manual |
| POST | `/api/google-calendar/citas/{id}/sincronizar` | ADMIN | Sincroniza cita con Calendar |
| DELETE | `/api/google-calendar/eventos/{eventId}` | ADMIN | Elimina evento de Calendar |
| GET | `/api/google-calendar/eventos?inicio=&fin=` | ADMIN | Lista eventos en rango |

**Flujo de sincronización**:
1. Carga la cita con su servicio y usuario
2. Calcula hora de fin: `fechaHora + duracionMinutos`
3. Crea evento en Google Calendar con el usuario como invitado (recibe notificación)
4. Persiste el `googleCalendarEventId` en la entidad `Cita`

Usa autenticación por **Service Account** (OAuth2) con scope `CALENDAR` (lectura y escritura).

### 4.7 Módulo Admin (`/api/admin`)

Panel de reportes y estadísticas. Todos los endpoints requieren `ROLE_ADMIN`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/admin/dashboard` | Resumen: totales de usuarios, citas, ingresos del mes |
| GET | `/api/admin/reportes/ingresos?inicio=&fin=` | Ingresos en un período |

---

## 5. Infraestructura

### 5.1 Migraciones Flyway

Ubicación: `src/main/resources/db/migration/`

| Versión | Archivo | Tabla creada |
|---|---|---|
| V1 | `V1__create_users.sql` | `users` |
| V2 | `V2__create_servicios.sql` | `servicios` |
| V3 | `V3__create_citas.sql` | `citas` |
| V4 | `V4__create_pagos.sql` | `pagos` |
| V5 | `V5__create_redes_sociales.sql` | `redes_sociales` |

Las migraciones son compatibles con H2 (modo PostgreSQL) y PostgreSQL real gracias a `MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE` en la URL de H2.

### 5.2 Docker Compose

Archivo: `docker-compose.yml`

Servicios incluidos para entorno local con PostgreSQL:

- **postgres**: imagen `postgres:16-alpine`, puerto `5432`, volumen persistente `postgres_data`, healthcheck integrado
- **pgadmin**: imagen `dpage/pgadmin4`, puerto `5050`, interfaz web para administrar la BD

Uso:
```bash
docker compose up -d        # levantar en background
docker compose down         # detener y eliminar contenedores
docker compose down -v      # también elimina el volumen de datos
```

### 5.3 Dockerfile

Multi-stage build para imagen de producción:

- **Stage build**: `eclipse-temurin:21-jdk-alpine` — compila y genera el JAR
- **Stage runtime**: `eclipse-temurin:21-jre-alpine` — imagen final más liviana (solo JRE)
- Puerto expuesto: `8080`

```bash
docker build -t clientes-api .
docker run -p 8080:8080 --env-file .env clientes-api
```

### 5.4 Variables de entorno

Archivo de referencia: `.env.example`

| Variable | Descripción | Default dev |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `DB_URL` | URL de conexión PostgreSQL | — (solo prod) |
| `DB_USERNAME` | Usuario de BD | — (solo prod) |
| `DB_PASSWORD` | Contraseña de BD | — (solo prod) |
| `JWT_SECRET` | Secreto Base64 para firmar JWT | valor hardcoded de dev |
| `JWT_EXPIRATION` | Duración del token en ms | `86400000` (24h) |
| `GOOGLE_CALENDAR_ENABLED` | Activa la integración | `false` |
| `GOOGLE_CALENDAR_CREDENTIALS_FILE` | Path al JSON de Service Account | `credentials.json` |
| `GOOGLE_CALENDAR_ID` | ID del calendario | `primary` |
| `GOOGLE_CALENDAR_TIMEZONE` | Zona horaria para los eventos | `America/Bogota` |

> El archivo `.env.example` se commitea al repositorio; el `.env` real **no** se commitea.

### 5.5 Pipeline CI — GitHub Actions

Archivo: `.github/workflows/ci.yml`

Se ejecuta en cada push y pull request a las ramas `main` y `develop`.

Pasos:
1. Checkout del código
2. Setup Java 21 (distribución Temurin) con caché de Maven
3. `./mvnw verify --no-transfer-progress` con perfil `dev`
4. Variables de entorno del CI: `SPRING_PROFILES_ACTIVE=dev`, `JWT_SECRET` desde GitHub Secrets
5. Sube los reportes de Surefire como artefacto (visibles en la pestaña Actions)

---

## 6. Documentación del código

Se añadió Javadoc a todos los archivos Java del proyecto siguiendo el estilo:

- **Javadoc de clase**: rol en la arquitectura y decisiones de diseño no evidentes
- **Javadoc de método**: comportamiento no obvio, `@throws`, `@param`/`@return` cuando aportan valor
- **Comentarios inline** (`//`): solo para lógica que sorprendería a un lector futuro

Módulos documentados:

- `auth` — 10 archivos (User, Role, UserRepository, DTOs, AuthService, AuthController, JwtUtil, JwtAuthFilter, JwtAuthFilter, SecurityConfig, UserDetailsServiceImpl)
- `common` — 3 archivos (ApiResponse, ResourceNotFoundException, GlobalExceptionHandler)
- `usuario` — 4 archivos
- `servicio` — 6 archivos
- `agendamiento` — 7 archivos
- `pago` — 8 archivos
- `redessociales` — 7 archivos
- `googlecalendar` — 5 archivos
- `admin` — 4 archivos

---

## 7. Problemas resueltos

### Lombok no procesa las anotaciones
**Síntoma**: errores de compilación — `getEmail()`, `builder()`, etc. no encontrados.  
**Causa**: Lombok necesita estar en `annotationProcessorPaths` del `maven-compiler-plugin`.  
**Solución**: añadido el bloque `<annotationProcessorPaths>` en `pom.xml`.

### Conflicto `getPassword()` en `User.java`
**Síntoma**: error de compilación — `User does not override abstract method getPassword()`.  
**Causa**: `@Data` genera un `getPassword()` que colisiona con la interfaz `UserDetails`.  
**Solución**: reemplazado `@Data` por `@Getter` + `@Setter` explícitos.

### Dependencia circular en el arranque
**Síntoma**: `BeanCurrentlyInCreationException` al iniciar.  
**Causa**: `JwtAuthFilter` necesita `UserDetailsService`, que estaba definido como `@Bean` dentro de `SecurityConfig`, que a su vez inyecta `JwtAuthFilter`.  
**Solución**: extraído `UserDetailsServiceImpl` como `@Service` independiente, eliminado el bean de `SecurityConfig`.

### Puerto 8080 en uso
**Síntoma**: `Web server failed to start. Port 8080 was already in use`.  
**Causa**: instancia anterior de la app corriendo en background.  
**Solución**: `Get-NetTCPConnection -LocalPort 8080` para encontrar el PID, `Stop-Process -Id <pid> -Force` para terminar el proceso.

### Google Calendar rompe el arranque sin credenciales
**Síntoma**: error al iniciar en dev porque no existe `credentials.json`.  
**Causa**: `GoogleCalendarConfig`, `GoogleCalendarService` y `GoogleCalendarController` se instancian siempre.  
**Solución**: añadido `@ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")` en las tres clases. Por defecto `false`, por lo que en dev y CI el módulo completo no se carga.

---

## 8. Notas técnicas

### Patrón soft delete
Los módulos `Usuario`, `Servicio` y `RedSocial` nunca borran registros físicamente. El campo `active` / `activo` pasa a `false`. Esto preserva la integridad referencial: las citas y pagos históricos siguen apuntando al usuario o servicio aunque esté desactivado.

### Cross-module service calls
Los módulos se comunican inyectando **servicios** (nunca repositorios de otro módulo):
- `CitaService` inyecta `ServicioService` (para cargar el servicio al crear una cita)
- `PagoService` inyecta `CitaService` (para validar la cita y cambiar su estado al pagar)
- `GoogleCalendarService` accede directamente a `CitaRepository` (solo para actualizar el `googleCalendarEventId`)

### `ApiResponse<T>` como envoltorio universal
Todos los endpoints devuelven `ApiResponse<T>`. El cliente siempre recibe la misma estructura:
```json
{
  "success": true,
  "message": "...",
  "data": { ... }
}
```

### H2 en modo PostgreSQL
La URL `jdbc:h2:mem:clientesdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH` permite que H2 interprete el SQL de las migraciones Flyway igual que PostgreSQL, evitando tener que mantener dos conjuntos de scripts.

### `@Builder.Default` en entidades
Cuando una entidad usa `@Builder` y tiene campos con valor por defecto (p.ej. `activo = true`), se debe anotar con `@Builder.Default`. Sin esta anotación Lombok ignora el valor del inicializador al construir vía builder y el campo queda en `false`/`null`.
