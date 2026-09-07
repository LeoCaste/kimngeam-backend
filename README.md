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

Para detenerlo: `docker compose down` (agregar `-v` si además querés borrar el
volumen `pgdata` y partir de una base vacía).

## Variables de entorno

La app lee la conexión a la base desde variables de entorno (ver
`src/main/resources/application.yml`), nunca hardcodeadas:

| Variable                | Default     | Descripción                          |
|--------------------------|-------------|---------------------------------------|
| `DB_HOST`                | `localhost` | Host de Postgres                      |
| `DB_PORT`                | `5432`      | Puerto de Postgres                    |
| `DB_NAME`                | `kimngeam`  | Nombre de la base                     |
| `DB_USER`                | `kimngeam`  | Usuario de la base                    |
| `DB_PASSWORD`            | *(sin default, requerida)* | Password de la base    |
| `SPRING_PROFILES_ACTIVE` | `dev`       | Perfil activo (`dev` / `prod`)        |
| `PORT`                   | `8080`      | Puerto HTTP de la app                 |

Con los defaults del `docker-compose.yml` de arriba, alcanza con exportar
`DB_PASSWORD`:

```bash
export DB_PASSWORD=kimngeam
```

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

## Correr la app

```bash
export DB_PASSWORD=kimngeam
./mvnw spring-boot:run
```

## Notas pendientes

- La dimensión de `corpus_chunk.embedding` (`VECTOR(1536)`) es un placeholder
  alineado a los embeddings de OpenAI; se ajustará (con una migración nueva)
  una vez decidido el proveedor de embeddings.
- El proveedor de LLM de traducción todavía no está decidido (candidatos:
  OpenRouter, Google AI Studio, Ofox AI). La integración quedará detrás de una
  interfaz propia para poder cambiarlo sin tocar el resto del código.
