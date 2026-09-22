# Turnera — Contexto del proyecto

> Este archivo es la **fuente de verdad para asistentes de código**. Leé este documento completo
> antes de tocar código. Se actualiza constantemente: requisitos, casos de uso, decisiones y estado.

---

## 1. Narrativa del sistema

Turnera es un sistema de **reserva de turnos para clases de guitarra**. Es el proyecto personal de
un profesor de guitarra que quiere dejar de coordinar horarios por WhatsApp: sus alumnos reservan,
cambian o cancelan turnos de forma autónoma, y él gestiona su disponibilidad en un solo lugar.

Hoy el sistema es **de un solo profesor**, pero la arquitectura está pensada para escalar a
**múltiples profesores / usuarios** si mañana cualquiera quiere usarla. Por eso:

- Toda entidad de negocio pertenece a un `Teacher` (dueño del turno o del alumno).
- No hay tablas "de texto libre": se busca normalizar datos (horarios, duración, franjas).
- El backend expone una API REST; el front es una SPA consumidora.

**Principio rector:** *resolver primero el caso del profesor único, sin cerrarle la puerta a
la multi-tenencia.*

---

## 2. Estado actual (roadmap vivo)

| Área | Estado |
|---|---|
| Estructura repo (front/back/db/compose) | ✅ Hecho |
| Dockerfiles multi-stage + docker-compose | ✅ Hecho |
| Backend: skeleton Spring Boot + `/api/health` | ✅ Hecho |
| Frontend: skeleton React + Vite + check de salud | ✅ Hecho |
| Conexión a Postgres (JPA/JDBC) | 🟡 Configurada, sin tablas todavía |
| Modelo de datos (tablas) | ⏳ **Por definir** (ver §6) |
| Autenticación / roles | ⏳ Fuera de alcance inicial |
| Módulo de turnos (CRUD + disponibilidad) | ⏳ Pendiente |
| Módulo de alumnos | ⏳ Pendiente |

Leyenda: ✅ listo · 🟡 en curso · ⏳ pendiente

---

## 3. Stack y arquitectura

```
┌─────────────┐   HTTP/JSON   ┌──────────────┐   JPA   ┌──────────┐
│  Frontend   │ ────────────▶ │   Backend    │ ──────▶ │ Postgres │
│ React+Vite  │   /api/*      │ Spring Boot  │         │   16     │
│  (Nginx)    │ ◀──────────── │  (Java 21)   │ ◀────── │          │
└─────────────┘               └──────────────┘         └──────────┘
```

- **Frontend:** React 19 (JS, sin TypeScript) creado con Vite. Sirve estáticos con Nginx y
  proxea `/api/*` hacia el backend. Dev local usa el proxy de Vite.
- **Backend:** Spring Boot 3.5.x, Java 21, Maven (con wrapper `mvnw`). Web + Data JPA +
  Validation + Actuator. `ddl-auto: update` durante la etapa temprana.
- **Base de datos:** PostgreSQL 16 en Docker (volumen persistente + healthcheck).
- **Orquestación:** Docker Compose con 3 servicios: `db`, `backend`, `frontend` (detalles en §8).

### Reglas de arquitectura para quien escriba código
1. El backend **no sirve views**: solo JSON bajo `/api/`.
2. El frontend **no llama a Postgres**: todo pasa por la API.
3. El frontend en dev apunta al proxy `/api` → `http://localhost:8080`.
4. En Docker, Nginx resuelve `backend` por nombre de servicio (`proxy_pass http://backend:8080;`).
5. Configuración sensible (credenciales) siempre por variables de entorno, nunca hardcodeada.

---

## 4. Estructura del repositorio

```
Turnera/
├── AGENTS.md            ← este archivo (contexto & requisitos)
├── docker-compose.yml   ← orquestación local (db + backend + frontend)
├── .env.example         ← plantilla de variables de entorno
├── .gitignore
├── frontend/            ← React + Vite (JS)
│   ├── Dockerfile       ← multi-stage: node:22 build → nginx serve
│   ├── nginx.conf       ← SPA + proxy /api → backend
│   └── src/             ← App, componentes (proximamente), estilos
└── backend/             ← Spring Boot 3.5 · Java 21 · Maven
    ├── Dockerfile       ← multi-stage: maven build → JRE 21 (usuario no-root)
    ├── pom.xml
    ├── mvnw / mvnw.cmd  ← Maven wrapper (no depende de instalación global)
    └── src/main/java/com/turnera/
        ├── TurneraApplication.java
        └── web/HealthController.java   ← GET /api/health
```

---

## 5. Requisitos funcionales (borrador inicial)

> Marcar con ✅ lo resuelto, con ⏳ lo pendiente y con ⚠️ lo en discusión.

### Disponibilidad del profesor
- ⏳ El profesor define los **días y horarios** en los que da clases (franjas de disponibilidad).
- ⏳ Define la **duración** de cada clase (ej. 45/60/90 min) y el hueco entre turnos.
- ⏳ Puede bloquear/desbloquear fechas puntuales (feriados, vacaciones).
- ⚠️ ¿El profesor tiene turnos "abiertos" (cualquiera reserva) o por franja fija? **A definir.**

### Gestión de alumnos
- ⏳ Alta/edición/baja de alumnos (nombre, contacto).
- ⚠️ ¿Los alumnos se crean solos (reserva anónima) o los carga el profesor? **A definir.**

### Reserva de turnos
- ⏳ El alumno (o el profesor en su nombre) reserva un turno en una franja libre.
- ⏳ El sistema **no permite doble reserva** en el mismo horario.
- ⏳ Cancelación de turnos (con regla de aviso mínimo ⚠️).
- ⏳ Reasignación/cambio de un turno a otra franja libre.
- ⚠️ Confirmación/recordatorio automático (mail/WhatsApp). **A definir.**

### Consultas
- ⏳ Vista de agenda del profesor (día/semana/mes).
- ⏳ Estado del turno: reservado · confirmado · cancelado · asistido / no asistió.

### Multi-usuario (futuro)
- ⏳ Profesionales múltiples, cada uno con sus alumnos y disponibilidad.
- ⏳ Autenticación y roles (profesor vs alumno).
- ⏳ Aislamiento de datos por profesor (multi-tenant).

---

## 6. Casos de uso (a refinar con el dueño)

1. **CU-01 Definir disponibilidad** — El profesor marca días/horarios en los que da clases.
2. **CU-02 Reservar turno** — Un alumno elige una franja libre y la reserva.
3. **CU-03 Cancelar turno** — El alumno (o el profesor) cancela un turno reservado.
4. **CU-04 Reprogramar turno** — Se mueve un turno a otra franja libre.
5. **CU-05 Ver agenda** — El profesor ve sus turnos del día/semana.
6. **CU-06 Gestionar alumno** — Alta/baja/edición de alumnos.
7. **CU-07 Registrar asistencia** — Marcar si el alumno asistió o no.

---

## 7. Modelo de datos — ⚠️ POR DEFINIR

> **No hay tablas creadas todavía.** Este apartado se va completando a medida que el dueño
> devela el dominio. Es el próximo gran paso del proyecto.

### Ideas preliminares (candidatas, no definitivas)
- `Teacher` — profesor (base para el futuro multi-tenant). `id, name, email, ...`
- `Student` — alumno. `id, teacher_id, name, phone, email, ...`
- `Availability` / `Slot` — franja genérica de disponibilidad. `teacher_id, weekday/time range`
- `Appointment` — turno concreto reservado. `slot/start/end, student_id, status, notes`
- `Status` del turno — enum: `RESERVED, CONFIRMED, CANCELED, ATTENDED, NO_SHOW`

### Preguntas pendientes para el dueño
- ¿Cuánto dura una clase por defecto? ¿Varía por alumno/nivel?
- ¿Un turno pertenece siempre a un alumno con cuenta, o puede haber "pase por caja" sin cuenta?
- ¿Hace falta historial de clases pasadas (para facturar/cobrar)? → esbozar tabla de pagos.
- ¿Zona horaria única o se manejan múltiples zonas?

**Cuando definas esto, actualizá esta sección y recién después se crean entidades/migraciones.**

---

## 8. Levantar el proyecto

### Docker Compose (recomendado)
```bash
cp .env.example .env        # solo la primera vez; ajustar credenciales
docker compose up --build   # levanta db + backend + frontend
```
- Frontend: `http://localhost:8081`
- API / health: `http://localhost:8081/api/health` o directo `http://localhost:8080/api/health`
- Postgres: `localhost:5432` (credenciales en `.env`)

### Desarrollo local (sin Docker)
- **Backend:** `./backend/mvnw spring-boot:run` (requiere Postgres local o ajustar `application.yml`).
- **Frontend:** `cd frontend && npm install && npm run dev` → `http://localhost:5173` (proxy a 8080).

### Comandos útiles
```bash
docker compose down         # apaga todo (sin borrar volumen)
docker compose down -v      # apaga y borra el volumen de la DB (¡ojo!)
docker compose logs -f backend
./backend/mvnw test
```

---

## 9. Convenciones

- **Ramas:** `main` (estable) y `dev` (integración). Worktrees/feature-branchs cuando hagan falta.
- **Idioma del código:** identificadores en inglés; textos de UI y este documento en español.
- **Commits:** mensajes claros en español o inglés, en presente imperativo. Un cambio = un commit.
- **Cambios al modelo de datos:** primero actualizar §7 de este archivo, después código/migración.
- No tipear secretos en código; usar variables de entorno (`.env` local, nunca comitear).