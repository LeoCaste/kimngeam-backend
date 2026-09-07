# Kimngeam-IA — Backend

Backend Spring Boot (Java 21) del traductor español↔mapudungun de UFRO. Expone la
API REST, persiste el corpus curado en PostgreSQL/pgvector y aloja el pipeline
RAG (`rag/`) que retroalimenta las traducciones.

## Requisitos

- JDK 21
- Docker (para Postgres local)

## Levantar Postgres con pgvector

```bash
docker compose up -d
```

Esto levanta `pgvector/pgvector:pg16` en `localhost:5432` con:

- DB: `kimngeam`
- usuario: `kimngeam`
- password: `kimngeam`

Para detenerlo: `docker compose down` (agregar `-v` si además quieres borrar el
volumen `pgdata` y partir de una base vacía).

## Variables de entorno

La app lee configuración sensible desde variables de entorno (ver
`src/main/resources/application.yml`), nunca hardcodeada:

| Variable                | Default     | Descripción                          |
|--------------------------|-------------|---------------------------------------|
| `JWT_SECRET`             | *(sin default, requerida)* | Clave HS256 para firmar JWT, ≥32 caracteres |
| `JWT_EXPIRATION_MS`      | `86400000`  | Vida del token en ms (24h)            |
| `DB_HOST`                | `localhost` | Host de Postgres                      |
| `DB_PORT`                | `5432`      | Puerto de Postgres                    |
| `DB_NAME`                | `kimngeam`  | Nombre de la base                     |
| `DB_USER`                | `kimngeam`  | Usuario de la base                    |
| `DB_PASSWORD`            | *(sin default, requerida)* | Password de la base    |
| `SPRING_PROFILES_ACTIVE` | `dev`       | Perfil activo (`dev` / `prod`)        |
| `PORT`                   | `8080`      | Puerto HTTP de la app                 |

`DB_PASSWORD` no tiene default en `application.yml` a propósito (ningún
secreto lo tiene, ver `CLAUDE.md`), pero para desarrollo local su valor es
simplemente `kimngeam`, el mismo que expone `docker-compose.yml` arriba. En
producción debe venir del entorno real, nunca reutilizar ese valor.

Spring Boot no lee archivos `.env` de forma nativa, así que hay que
exportarlas al entorno antes de correr la app. El repo trae un
`.env.example` versionado como plantilla; `.env` nunca se versiona
(está en `.gitignore`).

### Setup inicial

```bash
cp .env.example .env
```

Editar `.env` y completar `JWT_SECRET` con una clave generada (nunca usar el
placeholder del ejemplo):

```bash
openssl rand -base64 48
```

Pegar el resultado en `JWT_SECRET=` dentro de `.env`. Las variables de
Postgres del `.env.example` ya calzan con los defaults de
`docker-compose.yml` de arriba, así que no hace falta tocarlas para
desarrollo local.

### Correr desde terminal

Como Spring Boot no carga `.env` solo, hay que exportarlo a la shell antes
de levantar la app:

```bash
set -a && source .env && set +a
./mvnw spring-boot:run
```

`set -a` hace que todo lo que `source` defina se exporte automáticamente;
`set +a` lo desactiva después para no seguir exportando el resto de la sesión.

### Correr desde IntelliJ

IntelliJ no carga `.env` automáticamente. Configurar las variables en la
run configuration: **Run → Edit Configurations… → (tu configuración
Spring Boot) → Environment variables**, y pegar ahí el contenido de `.env`
(IntelliJ acepta pegar múltiples `CLAVE=valor` de una vez). Alternativamente,
instalar el plugin *EnvFile* y apuntarlo directamente al archivo `.env`.

## Migraciones (Flyway)

Las migraciones viven en `src/main/resources/db/migration/` y corren
automáticamente al levantar la app (`spring.flyway.enabled: true`). La primera
(`V1__init_schema.sql`) crea el esquema completo: usuarios, invitaciones,
traducciones, contexto cultural, validaciones y `corpus_chunk` (con su columna
`embedding vector`) para el pipeline RAG.

Para correrlas sin levantar la app completa:

```bash
./mvnw flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/kimngeam \
  -Dflyway.user=kimngeam \
  -Dflyway.password=kimngeam
```

## Notas pendientes

- La dimensión de `corpus_chunk.embedding` (`VECTOR(1536)`) es solo un
  placeholder de referencia (coincide con la dimensión de los embeddings de
  OpenAI, pero eso no implica que ese proveedor ya esté elegido); se ajustará
  (con una migración nueva) una vez decidido el proveedor de embeddings.
- El proveedor de LLM de traducción todavía no está decidido (candidatos:
  OpenRouter, Google AI Studio, Ofox AI). La integración quedará detrás de una
  interfaz propia para poder cambiarlo sin tocar el resto del código.
