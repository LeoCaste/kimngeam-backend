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
| `KIMNGEAM_CORPUS_PATH`   | *(vacío)*   | Ruta absoluta a la carpeta con las fuentes del corpus (ver abajo) |
| `KIMNGEAM_CORPUS_INGESTION_ENABLED` | `false` | Activa el job de carga del corpus a `corpus_chunk` |
| `KIMNGEAM_CORPUS_INGESTION_BATCH_SIZE` | `200` | Tamaño de lote al insertar chunks y generar embeddings |
| `KIMNGEAM_EMBEDDING_PROVIDER` | `ollama` | Proveedor de embeddings activo: `openrouter` u `ollama` (ver abajo) |
| `KIMNGEAM_EMBEDDING_OPENROUTER_BASE_URL` | `https://openrouter.ai/api/v1` | Base URL de OpenRouter (API compatible con OpenAI) |
| `OPENROUTER_API_KEY`    | *(sin default, requerida si `provider=openrouter`)* | API key de OpenRouter |
| `KIMNGEAM_EMBEDDING_OPENROUTER_MODEL` | *(sin default)* | Modelo de embeddings en OpenRouter, ej. `baai/bge-m3` |
| `KIMNGEAM_EMBEDDING_OPENROUTER_TIMEOUT` | `30s` | Timeout de las llamadas a OpenRouter |
| `KIMNGEAM_EMBEDDING_OPENROUTER_MAX_RETRIES` | `3` | Reintentos ante fallos de red transitorios |
| `KIMNGEAM_EMBEDDING_OLLAMA_BASE_URL` | `http://localhost:11434` | Base URL del servidor Ollama local |
| `KIMNGEAM_EMBEDDING_OLLAMA_MODEL` | *(sin default)* | Modelo de embeddings en Ollama, ej. `bge-m3` |
| `KIMNGEAM_LLM_PROVIDER` | `openrouter` | Proveedor del LLM de generación activo: `openrouter` u `ollama` (ver abajo) |
| `KIMNGEAM_LLM_OPENROUTER_BASE_URL` | `https://openrouter.ai/api/v1` | Base URL de OpenRouter (API de chat compatible con OpenAI) |
| `OPENROUTER_API_KEY`    | *(sin default, requerida si `provider=openrouter`)* | API key de OpenRouter (compartida con embeddings) |
| `KIMNGEAM_LLM_OPENROUTER_MODEL` | `google/gemini-3.8-flash` | Modelo de generación en OpenRouter |
| `KIMNGEAM_LLM_OPENROUTER_TIMEOUT` | `90s` | Timeout de las llamadas a OpenRouter (la prueba comparativa midió 3-90s según el modelo) |
| `KIMNGEAM_LLM_OPENROUTER_MAX_RETRIES` | `2` | Reintentos ante fallos de red transitorios |
| `KIMNGEAM_LLM_OLLAMA_BASE_URL` | `http://localhost:11434` | Base URL del servidor Ollama local |
| `KIMNGEAM_LLM_OLLAMA_MODEL` | *(sin default)* | Modelo de generación en Ollama, ej. `llama3.2` |
| `KIMNGEAM_LLM_OLLAMA_TIMEOUT` | `90s` | Timeout de las llamadas a Ollama |
| `KIMNGEAM_LLM_OLLAMA_MAX_RETRIES` | `1` | Reintentos ante fallos de red transitorios |
| `KIMNGEAM_LLM_PARSING_MAX_REINTENTOS` | `2` | Reintentos si la respuesta del LLM no parsea como el JSON esperado |
| `KIMNGEAM_TRADUCTOR_UMBRAL_SIMILITUD` | `0.5` | Bajo este score de similitud un chunk recuperado se descarta como referencia |
| `KIMNGEAM_TRADUCTOR_MAXIMO_ENTRADA` | `5000` | Largo máximo (caracteres) del texto a traducir; sobre esto, 400 |
| `KIMNGEAM_TRADUCTOR_UMBRAL_SEGMENTACION` | `200` | Bajo este largo se traduce completo; sobre él se parte por oraciones |
| `KIMNGEAM_TRADUCTOR_PESO_CHUNK_VALIDADO` | `1.5` | Cuánto más pesa un chunk validado frente a uno sin revisar en la confianza |
| `KIMNGEAM_TRADUCTOR_RATE_LIMIT_MAX_REQUESTS` | `10` | Solicitudes máximas a `/traductor/traducir` por IP dentro de la ventana |
| `KIMNGEAM_TRADUCTOR_RATE_LIMIT_VENTANA` | `1m` | Ventana de tiempo del límite anterior |
| `KIMNGEAM_TRADUCTOR_CONFIANZA_TECHO_SIMILITUD` | `0.72` | Score máximo de similitud medido empíricamente sobre el corpus (Fase 5) |
| `KIMNGEAM_TRADUCTOR_CONFIANZA_PESO_MAXIMO` | `0.5` | Peso del score máximo de los chunks frente al promedio al combinar evidencia |
| `KIMNGEAM_TRADUCTOR_CONFIANZA_CHUNKS_COBERTURA` | `3` | Chunks de respaldo que saturan el factor de cobertura en 1.0 |
| `KIMNGEAM_ADMIN_UMBRAL_SIN_ACTIVIDAD` | `30d` | Umbral para derivar `estado = "sin-actividad"` en `GET /admin/usuarios` |
| `KIMNGEAM_ADMIN_VALIDACIONES_RECIENTES_LIMITE` | `10` | Filas de `validaciones_recientes` en `GET /admin/usuarios/:id` |
| `KIMNGEAM_ADMIN_INVITACIONES_EXPIRACION` | `7d` | Vigencia del token de `POST /admin/usuarios/invitar` |
| `KIMNGEAM_ADMIN_DASHBOARD_ULTIMAS_VALIDACIONES_LIMITE` | `5` | Filas de `ultimas_validaciones` en `GET /admin/dashboard` |
| `KIMNGEAM_MAIL_PROVIDER` | `log` | Envío de invitaciones: `log` (no envía nada real) o `smtp` |
| `KIMNGEAM_MAIL_REMITENTE` | `no-reply@kimngeam.cl` | Remitente que ve el destinatario de la invitación |
| `SPRING_MAIL_HOST` / `SPRING_MAIL_PORT` / `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` | *(vacíos)* | SMTP estándar de Spring Boot, solo si `KIMNGEAM_MAIL_PROVIDER=smtp` |

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

## Fuentes del corpus (Fase 2)

Las fuentes del corpus (diccionario mapudungun-español, PDFs OCR,
transcripciones `.txt`, JSONL preprocesado) pesan ~240 MB y **no van en este
repo**. `KIMNGEAM_CORPUS_PATH` apunta a la carpeta donde vivan en tu máquina;
un clon limpio del repo no las trae, así que nunca se hardcodea una ruta
relativa. Contactar al equipo para conseguir esa carpeta.

El job de ingesta (`rag/ingestion`) lee todos los `*.jsonl` de esa carpeta,
uno por línea, con el shape:

```json
{"source_type":"translation_examples","source_ref":"...","contenido":"MAP: ...\nESP: ...","variante":null,"metadata":{},"validado":false}
```

Está desactivado por defecto (`KIMNGEAM_CORPUS_INGESTION_ENABLED=false`):
nunca debe correr solo por levantar la app. Al activarlo, embede solo las
líneas `ESP:` de cada `contenido` (el texto bilingüe completo igual se
persiste tal cual) y es re-ejecutable sin duplicar — por cada `source_type`
presente en los JSONL leídos borra los chunks existentes de ese tipo antes de
recargar, así que cambiar de modelo de embeddings solo implica volver a
correr el job.

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

## Proveedor de generación / LLM (Fase 3)

Candidatos evaluados, todos vía OpenRouter (API de chat compatible con
OpenAI): `google/gemini-3.8-flash` (default, el que más se acercó al material
validado), `google/gemini-3.1-flash-lite` (el más rápido, ~3s) y
`openai/gpt-5.4-mini` (declara incertidumbre explícitamente). Las llamadas
reales midieron latencias de 3 a 90 segundos según el modelo, de ahí el
timeout alto por defecto.

El sistema soporta dos proveedores intercambiables por configuración, ambos
detrás de la interfaz propia `TranslationLlmClient`
(`rag/generation/TranslationLlmClient`) — a diferencia de embeddings, esta
interfaz es propia (no la de Spring AI) porque futuros proveedores como Google
AI Studio no son compatibles con el protocolo de OpenAI:

- **`openrouter`** (default): proveedor de producción, vía
  `spring-ai-starter-model-openai` apuntado a la base-url de OpenRouter.
  Requiere `OPENROUTER_API_KEY`.
- **`ollama`**: corre local, SOLO para testeo — nunca para producción.
  Requiere tener Ollama corriendo y el modelo descargado. A diferencia del
  proveedor de embeddings, acá Ollama **no** es el default: un modelo de chat
  local chico produce mapudungun inutilizable, así que no sirve ni como
  referencia y solo obligaría a descargar un modelo de chat sin necesidad.

Se elige con `KIMNGEAM_LLM_PROVIDER`; solo se instancia el bean del proveedor
activo (mismo mecanismo que embeddings). Cada traducción persistirá el
identificador "proveedor:modelo" que generó la respuesta en
`traduccion.modelo_llm` — trazabilidad completa de cada traducción (ver
`traduccion_segmento` y `traduccion_fuente` en
`V5__traduccion_trazabilidad.sql`, que registran de qué segmentos se compone
cada traducción y qué chunks del corpus respaldaron cada uno, con qué score).

## Orquestación del traductor (Fase 3)

`traductor/TraductorService` es el servicio invocable que arma una traducción
completa, expuesto por `POST /traductor/traducir` (ver "Endpoints del
traductor" más abajo):

1. **Segmentación condicional** (`traductor/TextSegmenter`): bajo
   `KIMNGEAM_TRADUCTOR_UMBRAL_SEGMENTACION` el texto se traduce completo en un
   solo segmento (`orden = 1`, para que el modelo de datos sea uniforme);
   sobre ese largo se parte por oraciones. Sobre
   `KIMNGEAM_TRADUCTOR_MAXIMO_ENTRADA` el request se rechaza (400) — cada
   traducción cuesta dinero.
2. **Por cada segmento**: se recupera contexto con `CorpusRetrievalService` y
   se descartan los chunks bajo `KIMNGEAM_TRADUCTOR_UMBRAL_SIMILITUD` (meter
   contexto irrelevante confunde al modelo). Si ninguno lo supera, el
   segmento se marca `con_respaldo = false` y se traduce igual, sin contexto.
   El segmento anterior (original + traducido) se pasa como contexto en el
   prompt solo para continuidad ("ella lo hervía" necesita saber a qué se
   refiere "ella"), nunca para que el modelo lo vuelva a traducir.
3. **Prompting** (`rag/generation/TranslationPromptBuilder`): arma el prompt
   desde la plantilla versionada `prompts/segmento-traduccion.txt` — un
   recurso, no un string en el código, porque va a iterar mucho. No usa el
   `PromptTemplate` de Spring AI porque su motor ST4 usa `{}` como
   delimitador por defecto, lo que chocaría con las llaves literales del
   shape JSON de ejemplo que el prompt le muestra al modelo.
4. **Parsing con reintentos** (`rag/generation/TranslationResponseParser` +
   `SegmentTranslator`): limpia el bloque de markdown que varios modelos
   agregan alrededor del JSON pese a que el prompt pide JSON puro, y
   reintenta hasta `KIMNGEAM_LLM_PARSING_MAX_REINTENTOS` veces si la
   respuesta no parsea. Agotados los intentos, falla explícito — nunca
   devuelve una traducción a medias.
5. **Confianza derivada del retrieval, nunca pedida al LLM**
   (`traductor/ConfianzaCalculator`): la de un segmento combina, de los
   chunks que lo respaldaron, el score máximo con el promedio ponderado (un
   chunk validado por un académico pesa `KIMNGEAM_TRADUCTOR_PESO_CHUNK_VALIDADO`
   veces más que uno sin revisar — `KIMNGEAM_TRADUCTOR_CONFIANZA_PESO_MAXIMO`
   decide cuánto pesa el máximo frente al promedio); `null` si no tuvo
   respaldo. Ese score combinado se reescala contra
   `[KIMNGEAM_TRADUCTOR_UMBRAL_SIMILITUD, KIMNGEAM_TRADUCTOR_CONFIANZA_TECHO_SIMILITUD]`
   en vez de `[0, 1]`, y se multiplica por un factor de cobertura que satura
   en 1.0 al llegar a `KIMNGEAM_TRADUCTOR_CONFIANZA_CHUNKS_COBERTURA` chunks de
   respaldo. **Calibración empírica (Fase 5, item 8):** el score crudo de
   bge-m3 queda comprimido justo por encima del umbral — dos traducciones con
   respaldo muy distinto (5 chunks centrales del corpus vs. 2 tangenciales)
   daban confianzas casi indistinguibles (0.56 vs 0.51). Se midió el techo
   real sobre las 10 consultas de referencia de
   `CorpusRetrievalServiceIntegrationTest` (máximo observado: 0.7154) y se
   ajustó la fórmula con ese techo, el blend máximo/promedio y el factor de
   cobertura; con eso, los mismos dos casos dan 0.34 vs 0.05. Recalibrar
   `KIMNGEAM_TRADUCTOR_CONFIANZA_TECHO_SIMILITUD` si el corpus cambia
   sustancialmente. La global es el promedio de las de sus segmentos,
   ponderado por el largo de cada uno — un segmento sin respaldo aporta 0
   pero su largo igual cuenta, así que arrastra la confianza global hacia
   abajo en vez de ignorarse.
6. **Persistencia atómica** (`traductor/TraduccionPersistor`, transaccional y
   separado del resto a propósito: las llamadas de red de los pasos 2-4 no
   deben mantener una conexión de base de datos abierta): `traduccion` (con
   `modelo_llm`), un `traduccion_segmento` por segmento, un `traduccion_fuente`
   por cada chunk que lo respaldó, y `contexto_cultural` solo cuando
   `direccion = es-map`.

## Endpoints del traductor (Fase 3)

`POST /traductor/traducir` — auth opcional: si viene un `Bearer` válido, la
traducción se asocia al usuario y entra a su historial; si no viene, se
traduce igual de forma anónima (el filtro JWT nunca rechaza la request por
falta de token, solo la deja sin autenticar). Request y response siguen el
shape de `docs/API_CONTRACTS.md`, con un campo adicional deliberado:
`advertencias` — un array de strings, no vacío cuando **ningún** segmento
tuvo respaldo del corpus. Es una decisión de producto: el sistema declara
explícitamente lo que no sabe en vez de dejarlo pasar con solo una confianza
baja, y esa señal alimenta la cola de trabajo de los académicos.

`GET /historial` — requiere auth y rol `academico`; devuelve las traducciones
del usuario autenticado (por `usuario_id`, nunca por nombre — ver CLAUDE.md),
más recientes primero.

`POST /traductor/traducir` tiene rate limiting por IP
(`traductor/TraductorRateLimiter`, `RateLimitInterceptor`): una ventana fija
en memoria, simple y suficiente para una sola instancia — cada llamada
dispara retrieval y una llamada real al LLM, y el endpoint acepta requests
anónimas, así que es abusable. Si el servicio llega a correr en más de una
instancia, este límite necesita moverse a un backend compartido (ej. Redis).
Al superarlo, `429` con `code: "RATE_LIMITED"`.

## Validaciones (Fase 4)

Esta es la parte del proyecto que de verdad importa (ver CLAUDE.md): un
académico valida una expresión o una traducción completa, y ese aporte entra
directo al corpus como material de primera clase — sin eso, el traductor
nunca mejora.

`POST /validaciones/expresion` y `POST /validaciones/general` — requieren
auth y rol `academico` (403 si no). Se ligan a la traducción por
`traduccion_id` y al académico por `usuario_id`, nunca por nombre (ver
`docs/audit.md`). El campo `tipo` del contrato se deriva de si `expresion` es
`null`, nunca se persiste aparte.

`validaciones/ValidacionCorpusIndexer` es el corazón de la fase: al guardar
una validación, genera su embedding (mismo `EmbeddingModel` activo que la
ingesta) y la inserta en `corpus_chunk` como `source_type = expert_feedback`,
`validado = true`, `source_ref` apuntando a la validación,
`validado_por`/`validado_en` del académico y `modelo_embedding` con el
identificador `"proveedor:modelo"` — una sola validación basta, sin segunda
aprobación. `CorpusChunkEmbeddingWriter` (antes solo para la ingesta) pasó a
ser de uso general para esto.

Reversibilidad: un chunk de `expert_feedback` se desactiva
(`activo = false`), nunca se borra —
`ValidacionCorpusIndexer.revertir(validacionId)` implementa el mecanismo,
expuesto por `POST /admin/validaciones/:id/revertir` (Fase 5, ver abajo).

`ValidacionCorpusIntegrationTest` es la prueba de que el sistema cumple su
objetivo declarado: crea una validación real sobre un tema específico,
confirma los campos del `corpus_chunk` resultante, y una consulta de
retrieval sobre ese mismo tema la recupera — además confirma que la
confianza pondera más ese chunk validado que uno sin revisar, con datos
reales. Corre en el build normal (Postgres + Ollama, sin costo).

`V6__seed_variante.sql` siembra la tabla `variante` (`Nguluche`, `Chedungun`,
`Lafkenche`, `Pewenche`, las que ya usa el frontend prototipo — ver
`docs/audit.md`): existía desde la V1 como lookup referenciado por FK desde
`contexto_cultural`, `corpus_chunk` y `validacion`, pero nunca se había
sembrado porque hasta ahora ningún flujo real insertaba un `variante` no
nulo. `POST /validaciones/expresion` sí lo hace.

## Panel de administración (Fase 5)

Seis endpoints, todos bajo `/admin/**` con rol `admin` reforzado
server-side (`SecurityConfig`, desde Fase 1) — el frontend nunca es la
frontera de autorización (ver CLAUDE.md).

`GET /admin/usuarios` y `GET /admin/usuarios/:id` — listado y detalle vía
JDBC directo (`admin.usuarios.UsuarioAdminService`), mismo criterio que
`rag.retrieval.CorpusRetrievalService`: son agregados de solo lectura que no
ganan nada pasando por JPA. `estado` se deriva en cada consulta
(`EstadoUsuarioCalculator`), nunca se persiste: `"inactivo"` si el usuario
está marcado así, `"sin-actividad"` si está activo pero `ultimo_acceso` es
nulo o anterior a `KIMNGEAM_ADMIN_UMBRAL_SIN_ACTIVIDAD`, `"activo"` en otro
caso. El detalle trae las últimas `KIMNGEAM_ADMIN_VALIDACIONES_RECIENTES_LIMITE`
validaciones del usuario, unidas por `usuario_id` (nunca por nombre, ver
`docs/audit.md`).

`POST /admin/usuarios/invitar` — crea la fila en `invitacion` (token +
expiración `KIMNGEAM_ADMIN_INVITACIONES_EXPIRACION`) y despacha el correo
detrás de `shared/mail/MailSender`, con el mismo mecanismo de proveedor
intercambiable que embeddings y LLM (`KIMNGEAM_MAIL_PROVIDER`):
`LoggingMailSender` (default) **no envía nada real**, solo deja constancia
en el log — así un clon nuevo del repo prueba el flujo sin credenciales;
`SmtpMailSender` usa `spring-boot-starter-mail` y las propiedades estándar
`spring.mail.*` (`SPRING_MAIL_HOST`/`PORT`/`USERNAME`/`PASSWORD`) —
deliberadamente SMTP genérico, no la API de un proveedor específico, porque
lo habla tanto el correo institucional de la UFRO como los servicios
transaccionales.

`PATCH /admin/usuarios/:id/estado` — solo acepta `"activo"` / `"inactivo"`
(400 si se intenta asignar `"sin-actividad"`, que es derivado).

`GET /admin/validaciones` — filtros combinables `tipo`, `usuario_id`,
`busqueda`, siempre parametrizados con `?` (nunca concatenando el valor del
usuario en el SQL). `tipo` se deriva de si `expresion` es `null`
(`admin.validaciones.TipoValidacion`, compartido con `admin.usuarios` y
`admin.dashboard`).

`POST /admin/validaciones/:id/revertir` — no es parte de los 14 endpoints
originales del contrato: expone `ValidacionCorpusIndexer.revertir(...)`
(existía desde Fase 4 sin forma de invocarse).

`GET /admin/dashboard` — agregados sin tabla propia
(`admin.dashboard.DashboardService`). Dos métricas del contrato eran
ambiguas y se acordaron explícitamente con el usuario antes de implementar:

- **`expresiones`** (`total`, `con_contexto`, `sin_contexto`, `sin_validar`):
  cuatro conteos **independientes**, no una partición de `total`. `total`,
  `con_contexto` y `sin_contexto` se calculan sobre traducciones `es-map`
  (único sentido donde existe contexto cultural, ver CLAUDE.md); `sin_validar`
  se calcula sobre traducciones de **ambas** direcciones, porque una
  traducción `map-es` también puede validarse.
- **`variantes[].porcentaje`**: participación de esa variante sobre el total
  de validaciones (`cantidad / total_validaciones * 100`), **no** cobertura
  real del corpus. Hoy no es posible calcular cobertura real porque
  `corpus_chunk.variante` quedó `null` para los 321 chunks de transcripciones
  orales — los archivos fuente no traen marca dialectal (ver ingesta, Fase 2).
  Queda como decisión abierta en `docs/TODO.md` si en algún momento se anota
  esa variante retroactivamente.

`ultimas_validaciones` trae las últimas
`KIMNGEAM_ADMIN_DASHBOARD_ULTIMAS_VALIDACIONES_LIMITE` validaciones; su
`usuario` es `{nombre, inicial}`, sin `id` — shape distinto del `usuario` de
`GET /admin/validaciones` (ese sí trae `id`), tal como lo define el contrato
para cada endpoint.

## Notas pendientes

- Fase 6 (hardening / cierre) — ver `docs/TODO.md`.
