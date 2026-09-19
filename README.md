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
| `KIMNGEAM_EMBEDDING_PROVIDER` | `ollama` | Proveedor de embeddings activo: `openrouter` u `ollama` (ver abajo) |
| `KIMNGEAM_EMBEDDING_OPENROUTER_BASE_URL` | `https://openrouter.ai/api/v1` | Base URL de OpenRouter (API compatible con OpenAI) |
| `OPENROUTER_API_KEY`    | *(sin default, requerida si `provider=openrouter`)* | API key de OpenRouter |
| `KIMNGEAM_EMBEDDING_OPENROUTER_MODEL` | *(sin default)* | Modelo de embeddings en OpenRouter, ej. `baai/bge-m3` |
| `KIMNGEAM_EMBEDDING_OPENROUTER_TIMEOUT` | `30s` | Timeout de las llamadas a OpenRouter |
| `KIMNGEAM_EMBEDDING_OPENROUTER_MAX_RETRIES` | `3` | Reintentos ante fallos de red transitorios |
| `KIMNGEAM_EMBEDDING_OLLAMA_BASE_URL` | `http://localhost:11434` | Base URL del servidor Ollama local |
| `KIMNGEAM_EMBEDDING_OLLAMA_MODEL` | *(sin default)* | Modelo de embeddings en Ollama, ej. `bge-m3` |

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

## Seed de desarrollo (perfil `dev`)

Con el perfil `dev` activo (el default), al arrancar la app se crean
automáticamente dos usuarios si no existen ya, para poder probar los
endpoints de `/auth` sin pasar por `/auth/registro`:

| Rol        | Email               | Password      |
|------------|---------------------|---------------|
| academico  | `tefi@ufro.cl`      | `password123` |
| admin      | `admin@kimngeam.cl` | `admin1234`   |

Son credenciales de desarrollo, sin datos sensibles reales — no hay problema
en que estén documentadas acá. Este seed nunca corre bajo el perfil `prod`
(`DevUsuarioSeeder` está anotado con `@Profile("dev")`).

## Proveedor de embeddings (Fase 2)

Modelo elegido: **bge-m3** (`baai/bge-m3` en OpenRouter), dimensión 1024 (ver
`V4__corpus_chunk_embedding_dimension.sql`). El sistema soporta dos
proveedores intercambiables por configuración, ambos detrás de la
abstracción `EmbeddingModel` de Spring AI (`rag/embedding/EmbeddingModelConfig`):

- **`ollama`** (default): corre local, no consume créditos. Requiere tener
  Ollama corriendo y el modelo descargado (`ollama pull bge-m3`).
- **`openrouter`**: API externa de producción, compatible con el protocolo de
  OpenAI (por eso usa el starter `spring-ai-starter-model-openai` apuntado a
  la base-url de OpenRouter). Requiere `OPENROUTER_API_KEY`.

Se elige con `KIMNGEAM_EMBEDDING_PROVIDER`; solo se instancia el bean del
proveedor activo (las autoconfiguraciones propias de Spring AI para OpenAI y
Ollama están excluidas en `application.yml` para que nunca compitan con esa
elección). `corpus_chunk.modelo_embedding` guarda un identificador
"proveedor:modelo" (ej. `ollama:bge-m3` u `openrouter:baai/bge-m3`): el mismo
modelo servido por proveedores distintos no da necesariamente el mismo
vector, por eso el proveedor es parte del identificador.

## Notas pendientes

- El proveedor de LLM de traducción todavía no está decidido (candidatos:
  OpenRouter, Google AI Studio, Ofox AI). La integración quedará detrás de una
  interfaz propia para poder cambiarlo sin tocar el resto del código.
