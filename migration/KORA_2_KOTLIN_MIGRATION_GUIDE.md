# Kora 1.x → 2.x: руководство по миграции Kotlin-проектов

Самодостаточный документ: Kotlin-разработчику для миграции своего проекта не нужно открывать Java-гайд.
Правила, общие для двух языков, продублированы здесь целиком, с идиоматичными Kotlin-примерами.

Проверенная целевая версия — `2.0.0.RC2`; изменения после неё отмечены отдельно по commit ID.
Источники API — исходники и BOM выбранного релиза Kora, документация версии 2.x.
Примеры показывают форму изменения и не заменяют сборку конкретного проекта.

## Применение к своему проекту

Руководство не требует checkout примеров или соседнего checkout фреймворка. Выберите точный
релиз Kora 2.x, сохраните текущую сборку и определите собственные source sets, factory paths,
версии плагинов, processors и точки входа. Имена `Pet`, `Application`, `SimpleCache` в примерах —
иллюстрации; заменяйте их своими типами. Список шагов не означает, что проект использует все интеграции.

Скопируйте весь инструментарий в произвольный каталог. Нужен Python 3.11+.
В POSIX shell задайте `MIGRATION_TOOLS` — абсолютный путь каталога с `scripts/`,
`PROJECT_ROOT` — абсолютный путь вашего проекта. В PowerShell используйте `$tools` и `$project`:

```powershell
$tools = 'C:\tools\kora-migration'
$project = 'C:\work\your-service'
python "$tools\scripts\migrate_kora_2.py" --project-root "$project" --diff
```

Общие параметры каждого скрипта: `--project-root`, `[PATH ...]`, повторяемый `--exclude GLOB`,
`--diff`, `--apply`. Без `--apply` файлы не записываются. Без `--project-root` используется текущий
рабочий каталог. PATH задаются относительно корня и ограничивают обработку выбранными модулями/файлами;
root build и version catalog за этой областью не обновляются автоматически.

```shell
python "$MIGRATION_TOOLS/scripts/migrate_kora_2.py" --project-root "$PROJECT_ROOT" --target-version 2.0.0.RC2 --diff
python "$MIGRATION_TOOLS/scripts/migrate_api_renames.py" --project-root "$PROJECT_ROOT" --diff
python "$MIGRATION_TOOLS/scripts/migrate_config_keys.py" --project-root "$PROJECT_ROOT" --diff
python "$MIGRATION_TOOLS/scripts/migrate_resilient_specs.py" --project-root "$PROJECT_ROOT" --diff
```

Проверьте каждый diff и `REVIEW`, затем повторите нужные команды с `--apply`.
Нет отдельного этапа RC1 → RC2: существующие команды ведут 1.x проект в выбранную 2.x версию.
Generated/build/vendor каталоги не мигрируйте; добавьте свои исключения. Symlinks/junctions не обходятся.
Все создаваемые/переносимые файлы проверяются на коллизии до записи. При конфликте этап останавливается;
во время самой записи возможны IO-сбои, поэтому исходное состояние должно быть сохранено в VCS.

`migrate_kora_2.py` меняет Kora namespace/FQCN, импортированные типы и распознанные координаты
Gradle/Maven/catalog. `--kora-version-property frameworkVersion` задаёт ваше имя свойства версии.
SNAPSHOT сохраняется. Kotlin/KSP pins обновляются только с `--update-tooling`;
дополнительно доступны `--kotlin-version`, `--ksp-version`. Custom convention plugins,
version.ref/aliases, BOM management и непредусмотренные формы DSL проверяются вручную.
`--move-packages` переносит source paths `ru/tinkoff/kora`; произвольные пакеты приложения не меняются.

Простые имена меняются только при явном Kora import; comments/string literals в source не переписываются.
Wildcard/alias imports, receiver types и shadowing требуют анализа. Скрипты не удаляют глобально KAPT,
`@Nullable`, `@Nonnull`, `suspend`, не меняют произвольные `parameters`, имена классов приложения,
JUnit/MockK/GraalVM/OpenAPI plugin или JDK toolchain. Миграцию этих мест выполняйте по семантическим разделам.

Config stage знает `httpServer` / `openapi.management`; для custom paths задайте
`--http-server-path`, `--openapi-path`. Для каждого circuit breaker явно укажите
`--circuit-breaker-path resilient.circuitbreaker.<name>`. `db` не переименовывается автоматически:
сопоставьте путь со своей JDBC factory. YAML получает вложенный `system`; сложный/inline HOCON,
YAML anchors, source-embedded config и конфликтующие ключи требуют ручного переноса.

Resilient stage генерирует specs из импортированных Kora annotations в package исходника.
Для custom корня config задайте `--config-prefix`, для generated type prefix — `--spec-prefix`.
Индивидуальные config paths, общий spec для нескольких исходников и коллизии проверьте вручную.
API stage оставляет неоднозначные JSON/repository/extract вызовы на `REVIEW`, сохраняя чужие методы.

После правок перегенерируйте AP/KSP/OpenAPI/protobuf, выполните сборку своего проекта и его тесты.
Gradle: используйте wrapper проекта, `clean`, затем production/test compilation с `--no-build-cache`.
Maven: используйте wrapper проекта или установленный Maven, `clean compile test-compile`.
Для тестов Gradle сначала настройте `showStandardStreams(false)` и `events("failed")`.
Успех механического этапа не подтверждает runtime config, wire contracts и native image.


---

## 0. Порядок миграции проекта

1. `python "$MIGRATION_TOOLS/scripts/migrate_kora_2.py" --project-root "$PROJECT_ROOT"` (сначала dry-run, затем `--apply`) — Kotlin-файлы, Gradle DSL, ресурсы; перенос пакетных каталогов включается через `--move-packages`. Остальные скрипты применяются к API/config/specs по своей области.
2. OpenRewrite-рецепт (`openrewrite/`) Kotlin не трансформирует вовсе; если в проекте есть и Java-модули — см. `openrewrite/README.md`.
3. **Обязательно**: `clean` + первая сборка с `--no-build-cache` (см. §1.4).
4. `./gradlew <module>:kspKotlin --console=plain` — KSP отрабатывает раньше компиляции и даёт первую волну ошибок.
5. Применить семантические правила из этого документа.
6. Прогнать тесты модуля и только затем переходить к следующему.

---

## 1. Сборка и окружение

### 1.1 JDK и совместимость сборки

```kotlin
val targetJava = 25 // baseline RC2; согласуйте с выбранным релизом

kotlin {
    jvmToolchain(targetJava)
}
```

Используйте JDK, совместимый с выбранными Kora, Gradle/Maven и Kotlin. Baseline RC2 требует JDK 25
для запуска buildscript generator. Java preview API подключайте только при реальной необходимости
и с одинаковым JDK на compilation/test/runtime; сам переход Kora не требует включать preview.

Toolchain недостаточно: `io.koraframework:openapi-generator` попадает в **buildscript classpath**, который резолвится JVM самого Gradle. На JDK 21 конфигурация падает:

```
Dependency requires at least JVM runtime version 25. This build uses a Java 21 JVM.
```

**Проверка:** `JAVA_HOME=<compatible-JDK> ./gradlew projects` проходит, на JDK 21 — нет.

### 1.2 Kotlin и KSP

| Было | Стало |
|---|---|
| Kotlin `1.9.25` | `2.4.20` |
| KSP `1.9.25-1.0.20` | `2.3.12` |

Эти версии совпадают с теми, на которых собран сам фреймворк (version catalog исходников выбранного релиза Kora) — расхождение версий приведёт к трудноотлаживаемым падениям процессоров.

**KSP 2 больше не экспортирует тип `KspTask`.** Старая обвязка перестала компилироваться:

**Было:**

```kotlin
import com.google.devtools.ksp.gradle.KspTask

tasks.withType<KspTask>().configureEach { … }
```

**Стало:**

```kotlin
tasks.matching { it.name.startsWith("ksp") }.configureEach { … }
```

либо привязка к конкретной задаче по имени (`tasks.named("kspKotlin")`).

### 1.3 Координаты

| Было | Стало |
|---|---|
| `ru.tinkoff.kora` (groupId) | `io.koraframework` |
| BOM `ru.tinkoff.kora:kora-parent` | `io.koraframework:kora-bom` |
| `ru.tinkoff.kora:symbol-processors` | `io.koraframework:symbol-processors` |
| `json-module` | `json-common` |
| `cache-redis` | `cache-redis-lettuce` |
| `http-client-async` | **удалён** → `http-client-jdk` / `http-client-ok` |
| `ru.tinkoff.kora.experimental:s3-client-aws` | два разных артефакта: `io.koraframework:s3-client-aws` (только AWS SDK-обёртка) и `io.koraframework.experimental:s3-client-kora` (декларативный `@S3`, пакет `io.koraframework.s3.client.kora.annotation`) |

### 1.4 Первая сборка после смены пакетов — `clean` + `--no-build-cache`

**Симптом:** ошибки `package ru.tinkoff.kora.* does not exist` / `Unresolved reference` в файлах, которых нет в исходниках — пути ведут в `build/generated/…`.

**Причина:** задачи-генераторы (OpenAPI, protobuf) не удаляют прежний вывод, а ключ build-кеша не учитывает смену `apiPackage`/`modelPackage`; старый и новый пакеты сосуществуют в одном source set.

```shell
./gradlew clean --continue
./gradlew classes testClasses --continue --no-build-cache
```

**Проверка:** сравните generated output после clean и перегенерации без build cache; старых пакетов в нём быть не должно.

### 1.5 Выравнивание сторонних версий — отдельный класс отказов

Kora 2.0 подняла версии транзитивных библиотек, и модули, закрепившие свои, ломаются **в рантайме**,
а не при компиляции. Ниже — типичные конфликты закреплённых версий; определяйте действующий runtime classpath своего проекта.

| Библиотека | Версия в Kora 2.0 | Симптом при расхождении |
|---|---|---|
| gRPC | `1.83.1` | `AbstractMethodError: ... does not define or inherit an implementation of the resolved method 'buildClientTransportServers(List, MetricRecorder)'` |
| Flyway | `13.1.0` | `FlywayException: Unsupported Database: PostgreSQL 16.x` |
| Byte Buddy (через Mockito) | нужен ≥ поддерживающий Java 25 | `IllegalArgumentException: Java 25 (69) is not supported by the current version of Byte Buddy` |

**gRPC.** Достаточно, чтобы `grpc-inprocess` / `grpc-netty` в тестах были той же версии, что и
`grpc-core`, который приходит с `io.koraframework:grpc-server`. Закреплённая старая версия даёт
`AbstractMethodError` при построении сервера.

**Flyway.** С 10-й версии поддержка конкретных СУБД вынесена в отдельные артефакты, а
`io.koraframework:database-flyway` отдаёт только `flyway-core`. Приложение обязано добавить диалект
само, иначе падает на старте:

```groovy
implementation "io.koraframework:database-flyway"
implementation "org.flywaydb:flyway-database-postgresql:13.1.0"
```

**Mockito.** Нужен `mockito-core`, чей Byte Buddy понимает class file version 69. В Kotlin-модулях
это отдельная ловушка: `org.mockito.kotlin:mockito-kotlin` тянет свою, более старую `mockito-core`,
поэтому её версию задают явно рядом.

**MockK.** Старый `io.mockk:mockk:1.13.x` тянет Byte Buddy `1.14.x`; его transformer пишет
`i.m.p.j.t.InliningClassTransformer - Failed to transform class ... Java 25 (69) is not supported`.
Обновите MockK минимум до `1.14.9` (он тянет Java 25-совместимый Byte Buddy `1.18.x`). Не оставляйте
`-Dnet.bytebuddy.experimental=true` как постоянное решение: флаг только отключает защитную проверку
неподдерживаемой версии class-файла. Источник зависимости проверьте командой
`./gradlew <module>:dependencyInsight --dependency byte-buddy --configuration testRuntimeClasspath`.

**Проверять после миграции**, а не полагаться на компиляцию: все три отказа рантаймовые, а
Byte Buddy к тому же прячется внутри `Application graph failed to initialize with N errors`
без видимых suppressed-исключений (Gradle их не печатает — временно включите `junitXml.required`).

Gradle placeholder `environment(["": ""])` нужно удалить полностью. Пустое имя переменной окружения
недопустимо на Windows: test JVM не стартует с `CreateProcess error=87, The parameter is incorrect`.

### 1.6 Процессорам нужна полная перекомпиляция

При инкрементальной сборке процессор базы данных может прочитать интерфейс репозитория из
class-файла, где имена параметров синтетические, и сообщить:

```
error: SQL query placeholder has no matching method parameter:
    :id
  Available parameters:
    - :arg0
```

Исходный код при этом корректен — `--rerun-tasks` или `clean` на модуле убирает ошибку.
Сообщение на причину не указывает, поэтому при странных отказах процессоров начинайте с чистой сборки.

---

## 2. Пакеты и аннотации

Все пакеты фреймворка: `ru.tinkoff.kora.*` → `io.koraframework.*`.

DI-аннотации переехали на уровень глубже — в `annotation`:

| Было | Стало |
|---|---|
| `ru.tinkoff.kora.common.KoraApp` | `io.koraframework.common.annotation.KoraApp` |
| `ru.tinkoff.kora.common.Component` | `io.koraframework.common.annotation.Component` |
| `ru.tinkoff.kora.common.DefaultComponent` | `io.koraframework.common.annotation.DefaultComponent` |
| `ru.tinkoff.kora.common.KoraSubmodule` | `io.koraframework.common.annotation.KoraSubmodule` |
| `ru.tinkoff.kora.common.Mapping` | `io.koraframework.common.annotation.Mapping` |
| `ru.tinkoff.kora.common.Module` | `io.koraframework.common.annotation.Module` |
| `ru.tinkoff.kora.common.Tag` | `io.koraframework.common.annotation.Tag` |

```kotlin
@KoraApp
interface Application : HoconConfigModule, LogbackModule, UndertowPublicHttpServerModule

fun main() = KoraApplication.run(ApplicationGraph::graph)
```

---

## 3. Нуллабельность: убрать аннотации, использовать типы Kotlin

**Было (1.x):** в Kotlin-коде встречались `jakarta.annotation.Nullable`, в том числе как `@field:Nullable`.

**Стало:** нуллабельность выражается **типом** — `T?`. Java/JSpecify-аннотации из Kotlin-кода удаляются.

```kotlin
// Было
class UserRequest(@field:Nullable val name: String)

// Стало
class UserRequest(val name: String?)
```

**Причина:** JSpecify-аннотации являются type-use; `@field:Nullable` под Kotlin 2.4 — некорректная цель, а дублирование нуллабельности аннотацией и типом расходится с платформенными типами при взаимодействии со сгенерированным Java-кодом.

**Автоматизация:** REVIEW. Переносите nullability в Kotlin-типы вручную; импорт и аннотация могут относиться к Java/platform boundaries.

---

## 4. HTTP-сервер

| Было | Стало |
|---|---|
| `UndertowHttpServerModule` | `UndertowPublicHttpServerModule` |
| `http.server.common.HttpServerRequest` | `http.server.common.request.HttpServerRequest` |
| `http.server.common.HttpServerResponse` | `http.server.common.response.HttpServerResponse` |
| `http.server.common.HttpServerResponseException` | `http.server.common.response.HttpServerResponseException` |
| `http.server.common.HttpServerInterceptor` | `http.server.common.interceptor.HttpServerInterceptor` |
| `http.server.common.handler.HttpServerRequestMapper` | `http.server.common.request.HttpServerRequestMapper` |
| `http.server.common.handler.HttpServerResponseMapper` | `http.server.common.response.HttpServerResponseMapper` |
| `http.client.common.HttpClientResponseException` | `http.client.common.exception.HttpClientResponseException` |

Обработка HTTP синхронная: из контрактов уходят `suspend`, реактивные обёртки и явный Kora `Context` (он удалён из синхронных HTTP API — механической замены нет, сигнатура переписывается).

**Тег глобального интерцептора сменился:**

| Было | Стало |
|---|---|
| `@Tag(HttpServerModule::class)` | `@Tag(HttpServer::class)` |
| `import …http.server.common.HttpServerModule` | `import …http.server.common.HttpServer` |

В 2.0 фреймворк собирает глобальные интерцепторы по тегу `HttpServer` (`@Tag(HttpServer.class) All<HttpServerInterceptor>` в `HttpServerModule`).

**Коварство:** старый тег компилируется без ошибок — класс существует, просто по нему никто не ищет интерцепторы. Интерцептор молча перестаёт вызываться — ловится только тестом.

Интерцептор (сигнатура 2.0):

```kotlin
@Tag(HttpServer::class)
@Component
class HttpExceptionHandler(private val errorWriter: JsonWriter<MessageTO>) : HttpServerInterceptor {

    override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse =
        try {
            chain.process(request)
        } catch (e: HttpServerResponseException) {
            e
        } catch (e: Exception) {
            HttpServerResponse.of(500, HttpBody.json(errorWriter.toByteArray(MessageTO(e.message))))
        }
}
```

`JsonWriter.toByteArrayUnchecked(value)` удалён — используйте `toByteArray(value)`. В 2.0 этот метод не объявляет checked-исключений, поэтому блоки `catch (e: IOException)` вокруг него становятся лишними (в Kotlin это не ошибка компиляции, но мёртвый код; в Java — ошибка).

### Реактивные и `suspend`-контроллеры удалены

Для возврата `Mono`/`Flux` в `http-server-common` нет мапперов (проверено по исходникам) — такие методы переводятся на синхронный возврат `HttpServerResponse`.

Методы `@HttpController`/`@HttpRoute` также не должны оставаться `suspend`. Если отдельный
suspend controller только дублирует уже имеющийся синхронный пример, удалите controller и его
route-тест целиком. Иначе снимите `suspend` и синхронно перепишите всю вызывающую цепочку.
После удаления последнего coroutine usage удалите `kotlinx-coroutines-core`/`jdk8` из модуля.

OpenRewrite recipe `RemoveSuspendHttpServerMethods` и Python migrator снимают `suspend` в
файлах controller-контрактов. Удаление дубликатов, тестов и dependency требует проверки модуля.

**Диагностическая ловушка:** пока в графе остаётся неразрешимый компонент, сообщение об ошибке может указывать на другой, исправный компонент. Сначала убирайте код на удалённой функциональности, потом разбирайте остаток.

### HTTP-клиент: `suspend` API запрещён

В Kora 2.0 методы декларативного `@HttpClient` должны быть синхронными. Удалите `suspend`
из сигнатур клиента. Не оставляйте в том же интерфейсе default-обёртки вида
`suspend fun get(...) = withContext(Dispatchers.IO) { getBlocking(...) }`: это по-прежнему
публичный coroutine API HTTP-клиента и маскирует синхронный контракт.

```kotlin
// 1.x
suspend fun get(id: Long): Pet

// 2.0
fun get(id: Long): Pet
```

После замены уберите ставшие ненужными `runBlocking`/`runTest`, coroutine-тесты и импорты.
Если модуль больше не использует корутины, удалите также прямые зависимости
`org.jetbrains.kotlinx:kotlinx-coroutines-core` и `kotlinx-coroutines-jdk8`. Не удаляйте их
раньше переписывания всех usages, но и не оставляйте как финальное решение для structured
parallelism: `coroutineScope`/`supervisorScope`/`async`/`await` мигрируют по §13.
Если корутины действительно нужны бизнес-логике, мост размещается в отдельном application
service, но не в `@HttpClient` interface. Дублирующий suspend-only клиент лучше удалить целиком.

OpenRewrite recipe `RemoveSuspendHttpClientMethods` и Python migrator снимают `suspend` в
HTTP-client contracts. Переписывание вызывающей цепочки остаётся семантической проверкой.

### HTTP-клиент: `configPath` → `value`

**Было:**

```kotlin
@HttpClient(configPath = "httpClient.userApi")
interface UserApiClient { … }
```

**Стало:**

```kotlin
@HttpClient("httpClient.userApi")
interface UserApiClient { … }
```

**Причина:** в 2.0 аннотация объявлена как `String value() default ""` плюс `telemetryTag()` и `httpClientTag()`; атрибута `configPath` больше нет.

**Автоматизация:** детерминированная замена.

**Мапперы и интерцепторы обязаны быть DI-компонентами.** Генерируемый модуль контроллера инжектит их, а не создаёт:

```kotlin
@Component
class UserContextRequestMapper : HttpServerRequestMapper<UserContext> { … }
```

Симптом при отсутствии `@Component`: `No component found for dependency: … (no tags)`.

### Сигнатура `HttpServerResponseMapper` в Kotlin

Интерфейс во фреймворке помечен JSpecify (`@NullMarked` + `@Nullable` на втором параметре).
Kotlin проверяет нуллабельность переопределений строго, поэтому второй параметр обязан быть `T?`:

```kotlin
@Component
class PetResponseMapper : HttpServerResponseMapper<Pet> {
    override fun apply(request: HttpServerRequest, result: Pet?): HttpServerResponse { … }
}
```

С `result: Pet` компилятор выдаёт `'apply' overrides nothing` — сообщение об ошибке
не упоминает нуллабельность, поэтому его легко принять за неверный дженерик.

То же касается `@Mapping`-мапперов параметров и любых других переопределений контрактов Kora:
в 2.0 API размечено JSpecify, и Kotlin это видит, тогда как Java — нет. **Это асимметрия:
Java-модуль соберётся, Kotlin-двойник того же кода — нет.**

Метод, ничего не возвращающий, требует `VoidResponseMapper` — отдельного типа для `Unit`
в 2.0 нет, а `HttpServerResponseMapper<Unit>` не разрешается.

---

## 5. Конфигурация

### 5.1 Порты HTTP-сервера — молчаливый убийца старта

| Было (1.x) | Стало (2.0) |
|---|---|
| `httpServer.publicApiHttpPort` | `httpServer.port` |
| `httpServer.privateApiHttpPort` | `httpServer.system.port` |
| `httpServer.privateApiHttpReadinessPath` | `httpServer.system.readinessPath` |
| `httpServer.privateApiHttpLivenessPath` | `httpServer.system.livenessPath` |
| `httpServer.privateApiHttpMetricsPath` | `httpServer.system.metricsPath` |

```hocon
# Было                       # Стало
httpServer {                 httpServer {
  publicApiHttpPort = 8080     port = 8080
  privateApiHttpPort = 8085    system.port = 8085
}                            }
```

**Почему критично.** `SystemHttpServerConfig extends HttpServerConfig`, то есть системный сервер наследует `port()` = `8080`. Пока старый ключ не распознан, **оба** сервера берут 8080 и приложение падает на старте (`Address already in use`). Компиляция при этом зелёная: лишние ключи просто игнорируются.

**Автоматизация:** `python "$MIGRATION_TOOLS/scripts/migrate_config_keys.py" --project-root "$PROJECT_ROOT" --apply`.

### 5.2 Секция JDBC: `db` → `jdbc`

```hocon
# Было        # Стало
db {          jdbc {
  jdbcUrl =     jdbcUrl =
  username =    username =
}             }
```

**Причина:** `JdbcDatabaseModule` в 2.0 создаёт `new JdbcDatabaseFactoryModule("jdbc")`.

**Симптом:** компиляция зелёная, падение на старте: `ConfigValueException: … got null at path: 'ROOT.jdbc.username'`.

**Автоматизация:** для HTTP/OpenAPI — path-scoped правила; JDBC path меняется вручную по выбранной factory.

### 5.3 Остальное

| Было | Стало |
|---|---|
| `@ConfigValueExtractor` | `@ConfigMapper` |
| пакет `config.common.extractor` | удалён |

### 5.4 Телеметрия: метрики выключены по умолчанию

`TelemetryConfig.MetricsConfig.enabled()` в 2.0 возвращает `false`. Приложение стартует, эндпоинт
метрик отвечает `200`, но `http_server_*`, `http_client_*`, `db_*` и прочие метрики компонентов
в выводе отсутствуют — видны только JVM-метрики, которые регистрирует сам реестр.

```hocon
httpServer {
  telemetry.logging.enabled = true
  telemetry.metrics.enabled = true   # в 2.0 обязательно явно
}
```

Тот же ключ есть у каждого компонента с телеметрией (`httpClient.<name>.telemetry.metrics.enabled`,
`db.telemetry.metrics.enabled` и т. д.). Логирование (`logging.enabled`) тоже `false` по умолчанию,
трассировка (`tracing.enabled`) — `true`.

**Молчаливый отказ:** ничего не падает и ничего не пишется в лог, метрики просто не собираются.

---

## 6. Отказоустойчивость: строковые имена → типизированные спецификации

**Было:**

```kotlin
@CircuitBreaker("pet")
@Retry("pet")
@Timeout("pet")
fun findById(petId: Long): PetWithCategory?
```

**Стало:**

```kotlin
@CircuitBreakable(PetCircuitBreaker::class)
@Retryable(PetRetry::class)
@Timeout(PetTimeouter::class)
fun findById(petId: Long): PetWithCategory?

@CircuitBreakerSpec("resilient.circuitbreaker.pet")
interface PetCircuitBreaker : CircuitBreaker

@RetrySpec("resilient.retry.pet")
interface PetRetry : Retry

@TimeoutSpec("resilient.timeout.pet")
interface PetTimeouter : Timeouter
```

| Было | Стало |
|---|---|
| `@CircuitBreaker("name")` | `@CircuitBreakable(X::class)` |
| `@Retry("name")` | `@Retryable(X::class)` |
| `@Timeout("name")` | `@Timeout(X::class)` |
| `@Fallback(value = "name", method = "…")` | `@Fallback(method = "…")` |

Конфигурация: `slidingWindowSize` → `countBased.windowSize`, для окна фиксированного размера обязателен `type = FIXED_WINDOW`.

**Спецификации по умолчанию тоже типизированы.** Если раньше аннотация без имени брала секцию
`default`, теперь нужен явный тип из `io.koraframework.resilient.*`:

```kotlin
@Timeout(DefaultTimeouter::class)
@Retryable(DefaultRetry::class)
@CircuitBreakable(DefaultCircuitBreaker::class)
fun call(): String
```

**Именованная секция больше не наследует `default`.** В 1.x `resilient.circuitbreaker.my_cb`
дополняла `resilient.circuitbreaker.default`; в 2.0 секции независимы, и незаполненные поля
берутся из значений по умолчанию **самого типа**, а не из соседней секции. Практический эффект:
конфигурация, где в `my_cb` указан только один параметр, в 2.0 ведёт себя иначе — секцию нужно
заполнить целиком. Секция `default`, на которую больше никто не ссылается, становится мёртвой.

**Предикат отказа сменил интерфейс:**

| Было | Стало |
|---|---|
| `CircuitBreakerFailurePredicate` | `CircuitBreakerPredicate` |
| `test(throwable)` | `isCircuitBreakerFailure(throwable)` |

Свой предикат подключается тегом спецификации, к которой он относится:

```kotlin
@Component
@Tag(DefaultCircuitBreaker::class)
class MyPredicate : CircuitBreakerPredicate {
    override fun isCircuitBreakerFailure(throwable: Throwable): Boolean = throwable !is IllegalArgumentException
}
```

**Важно для Kotlin:** пока строковая форма остаётся в коде, KSP-процессор resilient падает с
`java.lang.ClassCastException: java.lang.String cannot be cast to com.google.devtools.ksp.symbol.KSType`
вместо внятной диагностики. Это подтверждённая связка «немигрированный код → краш процессора» (см. журнал подтверждённых проблем выбранного релиза).

**Автоматизация:** `migrate_resilient_specs.py` создаёт specs при явном Kora import; custom paths, expressions и коллизии требуют ручного решения.

---

## 7. Кеш

| Было | Стало |
|---|---|
| `@Cacheable(value = X::class, parameters = "id")` | `@Cacheable(value = X::class, args = "id")` |
| `@CacheInvalidate(value = X::class, invalidateAll = true)` | `@CacheInvalidateAll(X::class)` |
| `RedisCacheModule` | `LettuceRedisCacheModule` (артефакт `cache-redis-lettuce`) |

`RedisCacheModule` в 2.0 существует (`cache-redis-common`), но транспортно-нейтрален и сам `RedisCacheClient` не предоставляет.

Классы-мапперы ключей, указанные в `@Mapping`, должны быть `@Component` — включая вложенные классы.

---

## 8. База данных и `suspend`

- `database.jdbc.EntityJdbc` → `database.jdbc.annotation.EntityJdbc`.
- Контракты репозиториев в 2.0 **синхронные**. `suspend`-репозитории и корутинные контракты удалены.
- Если suspend repository был отдельным дубликатом уже существующего sync repository, удалите
  его целиком вместе с DI-ссылками и отдельными coroutine-тестами. Не создавайте второй
  синхронный интерфейс с теми же запросами.

**Было:**

```kotlin
@Repository
interface PetRepository : JdbcRepository {
    suspend fun findById(id: Long): Pet?
}
```

**Стало:**

```kotlin
@Repository
interface PetRepository : JdbcRepository {
    fun findById(id: Long): Pet?
}
```

**Это не механическое удаление `suspend`.** Снятие `suspend` распространяется вверх по цепочке вызовов: сервисы, контроллеры, тесты. Меняются отмена (structured concurrency больше не отменяет операцию БД), границы транзакций и распространение исключений. Тесты на `runTest`/`runBlocking` перестают быть нужны там, где вызовы стали синхронными.

Сохранять `suspend` «ради стиля» поверх синхронного контракта не следует. Если собственный
application-код использовал Kotlin Structured Concurrency для параллелизма, его также нужно
перевести на Java Structured Concurrency по правилам §13, а не оставлять coroutine-мост.

После удаления последнего coroutine usage удалите из модуля прямые зависимости
`kotlinx-coroutines-core`/`kotlinx-coroutines-jdk8`. Если корутины используются собственной
бизнес-логикой вне repository-контракта, зависимость сохраняется.

### Ручные транзакции

`executor().inTx { … }` с лямбдой Kotlin больше не выводится: перегрузок стало несколько,
и компилятор не выбирает между ними. Нужен явный SAM-конструктор:

```kotlin
// значение возвращается
val pet = repository.executor().inTx(JdbcExecutor.SqlSupplier {
    repository.insert(name)
})

// ничего не возвращается
repository.executor().inTx(JdbcExecutor.SqlRunnable {
    repository.deleteAll()
})
```

Без явного `SqlSupplier`/`SqlRunnable` ошибка выглядит как `Cannot infer type for type parameter T`
или `Overload resolution ambiguity` и на транзакции не указывает.

**Мапперы результатов и колонок принимают nullable.** `@Mapping`-классы для строк и колонок
переопределяют JSpecify-размеченные контракты, поэтому параметры объявляются как `T?`
— та же ловушка, что с `HttpServerResponseMapper` (§4).

**Удалённые интеграции:** R2DBC и Vert.x (см. §12).

---

## 9. Планировщик

**Было:** `@ScheduleWithTrigger(Tag(MyJob::class))`
**Стало:** `@ScheduleWithTrigger(MyJob::class)`

---

## 10. OpenAPI-генерация

Остались режимы `kotlin-client` и `kotlin-server`:

| Было | Стало |
|---|---|
| `kotlin-suspend-client` | `kotlin-client` |
| `kotlin-suspend-server` | `kotlin-server` |
| `kotlin-reactive-server` | `kotlin-server` |

Смена режима необходима, но недостаточна — сгенерированный код синхронный, и вызывающий код (делегаты, сервисы, тесты) нужно приводить к синхронным сигнатурам. Типичные ошибки после смены режима: `Cannot infer type for type parameter`, `'X' overrides nothing`, `Annotation argument must be a compile-time constant`.

### 10.1 Путь конфигурации клиента: первая буква строчная

Генератор 2.0 выводит путь конфигурации из имени API, приводя первую букву к строчной:
`PetApi` → `petApi`. Итоговый ключ — `<префикс>.<клиент>.<api>`:

```hocon
# Было
httpClient.petV2.PetApi { url = … }

# Стало
httpClient.petV2.petApi { url = … }
```

**Молчаливый отказ.** Секция со старым именем просто не читается: клиент поднимается без `url`,
и тесты не падают с внятной ошибкой — они висят на попытках запроса до таймаута. Ключ стоит
сверять со сгенерированным `@HttpClient`, а не подбирать.

### 10.2 Конструкторы моделей упорядочены по обязательности

Генератор 2.0 ставит required-поля первыми, а необязательные — после. Порядок аргументов
у сгенерированных TO меняется по сравнению с 1.x, причём **совместимо по типам**, поэтому
позиционный вызов может собраться и молча перепутать значения.

Единственный безопасный вариант — именованные аргументы:

```kotlin
return PetTO(status = status, id = pet.id, name = pet.name, category = asDTO(pet.category))
```

### 10.3 Enum ищется по wire-значению, а не по имени константы

У сгенерированных enum есть поле с исходным значением из спецификации, и оно может не совпадать
с именем константы (`available` против `AVAILABLE`, значения через дефис и т. п.).
`Enum.valueOf(...)` / `enumValueOf(...)` для разбора приходящего значения — ошибка:

```kotlin
// неверно: падает на любом значении, не совпадающем с именем константы
val status = PetTO.StatusEnum.valueOf(raw)

// верно: используем метод, сгенерированный Kora OpenAPI 2.0
val status = PetTO.StatusEnum.fromValue(raw)
```

Проявляется только на данных, компиляция чистая.

### 10.4 Теги требований безопасности называются по security-схеме

В Kora OpenAPI 2.0 генератор создаёт вложенный tag-класс из имени схемы в
`components.securitySchemes`, приводя его к PascalCase. Старые порядковые
`ApiSecurity.SecurityRequirementTagN` и варианты с маленькой буквы больше не подходят.

```kotlin
@Tag(ApiSecurity.ApiKeyAuth::class)
fun apiKeyExtractor(config: DataApiAuthConfig): HttpServerPrincipalExtractor<String, Principal> =
    HttpServerPrincipalExtractor { _, value -> … }
```

Тип второго параметра — то, что схема извлекает из запроса (`String` для apiKey/bearer),
первого результата — `Principal`.

### 10.5 Управление OpenAPI-эндпоинтом

| Было | Стало |
|---|---|
| `openapi.management.file` | `openapi.management.files` (список) |
| `openapi.management.rapidoc` | `openapi.management.scalar` |

Ключ `file` в 2.0 не читается — эндпоинт молча отдаёт пустую спецификацию.

### 10.6 `suspend` на сгенерированных клиентах не поддерживается

Режим `kotlin-client` порождает синхронные методы, и пометить их `suspend` нельзя.
Если вызывающий код обязан остаться корутинным, мост делается на своей стороне:

```kotlin
suspend fun findPet(id: Long): PetTO = withContext(Dispatchers.IO) { petApi.getPetById(id) }
```

Это осознанное решение, а не механическая замена: под виртуальными потоками
`Dispatchers.IO` обычно не нужен (§13).

### 10.7 `ValidationModule` тянет http-server-common

Подключение `ValidationModule` в клиентском приложении добавляет в граф зависимость
на `http-server-common` (через `ViolationExceptionHttpServerResponseMapper`). Для приложения
без HTTP-сервера это лишний артефакт в classpath; обходится добавлением зависимости,
поскольку разделить модуль на стороне приложения нельзя.

---

## 11. Тестирование

Общую версию JUnit обновите до `6.1.3`:

```properties
junitVersion=6.1.3
```

Используйте `testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))`.
`org.testcontainers:junit-jupiter` — интеграция Testcontainers, её версия независима от JUnit BOM.

Артефакт тестирования — `io.koraframework:test-junit5`. Основной процессор подключается как
`ksp("io.koraframework:symbol-processors:${property("koraVersion")}")`; test-конфигурация процессора
нужна только тестам, которые сами генерируют Kora graph. Не удаляйте `kspTest` массовой
заменой: сначала найдите все `@KoraApp`, объявленные именно в `src/test`.
Например, `@KoraAppTest(TestApplication::class)` для `TestApplication`, объявленного в
`src/test`, требует `kspTest("io.koraframework:symbol-processors:${property("koraVersion")}")`.

Пакет расширения: `io.koraframework.test.extension.junit5.*` — `@KoraAppTest`, `@TestComponent`, `KoraAppTestConfigModifier`, `KoraConfigModification`.

Тесты, написанные на `runTest` вокруг `suspend`-репозиториев, после перехода на синхронные контракты упрощаются до обычных тестов. MockK-моки `coEvery` заменяются на `every`.

В Kotlin-модулях не создавайте отдельную конфигурацию `koraBom` и не связывайте её через
`extendsFrom`. BOM подключается прямо к `implementation`, а версия процессора указывается явно:

```kotlin
dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))
    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")
}
```

`kspTest("io.koraframework:symbol-processors:${property("koraVersion")}")` добавляется только
когда тестовые исходники действительно требуют генерации графа, например содержат отдельный
`@KoraApp`. Проверьте каждый test source set, который объявляет собственный граф.

---

## 11a. Отдельные интеграции

### Kafka

Ридер `JsonReader<T>.read(data)` в 2.0 объявлен возвращающим nullable, поэтому в Kotlin
результат не присваивается non-null типу напрямую:

```kotlin
val event = requireNotNull(reader.read(data))
```

Слушатели телеметрии (`KafkaConsumerLoggerFactory` и подобные пользовательские фабрики) в 2.0
не подключаются как раньше; в примерах они удалены, а телеметрия настраивается конфигурацией.

### Camunda Zeebe

| Было | Стало |
|---|---|
| `ZeebeClient` | `CamundaClient` |
| `…camunda.zeebe.worker.JobWorkerException` | `…camunda.zeebe.worker.exception.JobWorkerException` |
| `zeebe.client.broker.gatewayAddress` | `zeebe.client.rest.url` |

Клиент в тестах инжектится напрямую, без `@TestComponent` на поле: с аннотацией он подменяется
моком и воркеры не срабатывают.

### S3

Артефакт 1.x `experimental:s3-client-*` разделён на два, и нужны **оба**:
`io.koraframework:s3-client-aws` (или `-minio`) даёт транспорт, а
`io.koraframework.experimental:s3-client-kora` — декларативный `@S3` и его KSP-процессор.
Подключение только первого компилируется, но `@S3`-интерфейсы не обрабатываются.

Инициализатор бакета должен быть корнем графа (`@Root`), иначе он выпадает из графа
как никем не используемый и бакет не создаётся — тесты падают на первой операции.

### Конфигурация в библиотечном модуле

`@ConfigMapper` (бывш. `@ConfigValueExtractor`) обрабатывается KSP, поэтому модуль-библиотека,
объявляющая конфиг-интерфейсы, обязан сам подключать KSP-плагин и `ksp("io.koraframework:symbol-processors")`.
В 1.x такие модули часто обходились без процессора, и после миграции ошибка выглядит как
отсутствие сгенерированного `*Module` у потребителя, а не у библиотеки.

---

## 12. Удалённая и несовместимо изменённая функциональность

| Функциональность 1.x | Состояние в 2.0 | Что делать |
|---|---|---|
| R2DBC (`database-r2dbc`) | удалён | переводить на синхронный `database-jdbc` |
| Vert.x SQL (`database-vertx`) | удалён | то же |
| `suspend`-репозитории | удалены | синхронные методы |
| корутинные контракты в модулях Kora | удалены | синхронные контракты |
| реактивные контракты репозиториев | удалены | синхронные контракты |
| `http-client-async` | удалён | `http-client-jdk` / `http-client-ok` |
| `@ConfigValueExtractor` | переименован | `@ConfigMapper` |
| `config.common.extractor` | пакет удалён | замена в `io.koraframework.config.common` |
| `Context` в синхронных HTTP API | удалён | переписать сигнатуру |
| `toByteArrayUnchecked` | удалён | `toByteArray` |
| `invalidateAll = true` | удалён | `@CacheInvalidateAll` |
| `KspTask` (тип Gradle) | удалён в KSP 2 | привязка задач по имени |

Модули примеров, зависящие от удалённого, не удалены: они исключены из агрегатной сборки и помечены `BLOCKED_BY_REMOVED_FUNCTIONALITY` в `KORA_2_MIGRATION_STATUS.md`.

---

## 13. Синхронная модель, Virtual Threads и Java Structured Concurrency

Kora 2.0 исполняет синхронные контракты на виртуальных потоках. Kotlin-код, который был
`suspend` только ради неблокирующего доступа к БД или HTTP, переводится в синхронный.

### 13.1 Обязательная замена Kotlin Structured Concurrency

Для `coroutineScope`, `supervisorScope`, `async`/`await`, `awaitAll`, дочерних `launch`,
structured cancellation и timeout scopes сначала определите требуемую семантику.
Варианты — синхронное выполнение, параллелизм на virtual threads или Java Structured Concurrency.
Переход Kora не требует применять `StructuredTaskScope` во всех application services.
Coroutine dependencies удаляются только после проверки всех оставшихся usages проекта.

Если выбран Java preview API, закрепите JDK, совместимые plugins и одинаковый major на
compile/test/runtime. API `StructuredTaskScope` зависит от конкретной версии JDK;
нельзя смешивать factories, `Joiner` и тип результата `join()` из разных preview итераций.

Ниже — пример формы решения для JDK 26. Сверьте его с API выбранного JDK перед использованием;
для baseline RC2 без этого сценария достаточно поддерживаемого JDK 25.

```kotlin
import java.util.concurrent.Callable
import java.util.concurrent.StructuredTaskScope

fun getDashboard(userId: Long): Dashboard =
    StructuredTaskScope.open(
        StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow<Any>(),
    ).use { scope ->
        val profile = scope.fork(Callable { profileClient.getProfile(userId) })
        val recommendations = scope.fork(Callable { recommendationClient.getForUser(userId) })

        scope.join()
        Dashboard(profile.get(), recommendations.get())
    }
```

Выбор policy переносится явно:

- fail-fast и отмена siblings при ошибке → `awaitAllSuccessfulOrThrow()`;
- первый успешный результат → актуальный `Joiner` из API выбранного JDK;
- supervisor-подобная семантика → `awaitAll()` и явная проверка `Subtask.state()`/`exception()`;
- `withTimeout` → timeout в `StructuredTaskScope.Configuration`;
- cancellation и exception propagation обязательно покрываются тестами заново.

### 13.2 Preview должен быть включён везде

Одного флага на компиляции недостаточно. `--enable-preview` обязателен при компиляции и на
каждом JVM-запуске: tests, `JavaExec`, application distribution/production command. Версия
`--release` обязана совпадать с выбранным JDK.

```kotlin
val targetJava = 26 // только если выбран JDK 26 и совместимый preview API

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJava))
}

kotlin {
    jvmToolchain(targetJava)
    compilerOptions {
        // Kotlin sources also use the selected JDK API and emit preview bytecode.
        freeCompilerArgs.addAll(
            "-Xjdk-release=$targetJava",
            "-Xjvm-enable-preview",
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJava)
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test>().configureEach {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}

application {
    // Merge with existing arguments instead of dropping application-specific JVM flags.
    applicationDefaultJvmArgs = listOf("--enable-preview", "-Dfile.encoding=UTF-8")
}
```

Для прямого запуска: `java --enable-preview -jar application.jar`. Образ контейнера и runtime
обязаны использовать тот же major JDK, которым скомпилирован preview-код.

Checklist перед merge:

1. Закрепить совместимую версию JDK и одинаковый major для toolchain/CI/container runtime.
2. Открыть JEP и Javadoc Structured Concurrency именно выбранного JDK; перепроверить `open`,
   `Joiner`, тип результата `join`, timeout и exception types.
3. Собрать main/test Kotlin и Java с preview; запустить unit/integration tests с preview.
4. Проверить production launcher, Docker/Kubernetes command и IDE run configurations:
   в каждом месте должен быть `--enable-preview`.
5. Удалить `kotlinx-coroutines-*`, только когда repository-wide audit больше не находит usages.

Это семантическая миграция: OpenRewrite/Python не могут автоматически выбрать `Joiner`, timeout,
failure/cancellation policy или восстановить supervisor semantics. Automation только находит и
удаляет запрещённые Kora contracts; structured concurrency переписывается разработчиком.

Изменения, которые нужно продумать при снятии `suspend`: отмена, границы транзакций, распространение исключений, необходимость `Dispatchers.IO` (не нужен — виртуальные потоки), тесты.

---

## 15. GraalVM Native Image

В репозитории нет Kotlin-модулей с native-сборкой — все три GraalVM-примера на Java. Поэтому
ниже явно разделено: §15.1–§15.7 — правила, **проверенные сборкой и запуском** образов и не
зависящие от языка (это конфигурация Gradle и метаданные самого фреймворка), §15.8 — то, что
специфично для Kotlin; измерьте это в своём приложении.

Окружение проверки: GraalVM CE 25.2.4+7.1 (`native-image 25.0.4`), установка —
`sdk install java 25.2.4-graalce`; образ для сборки в Docker — `ghcr.io/graalvm/native-image-community:25`.

### 15.1. Плагин `org.graalvm.buildtools.native`: 0.11.5 → 1.1.7

**Было / Стало** — в корневом build-файле:

```groovy
id "org.graalvm.buildtools.native" version "1.1.7" apply false   // было 0.11.5
```

**Причина:** на Gradle 9 `collectReachabilityMetadata` падает на резолве конфигурации из чужого
проекта: до 1.1.6 плагин регистрировал один shared build service `nativeConfigurationService`
на весь build, и все GraalVM-модули кроме первого получали сервис из чужого контекста.
В 1.1.6 имя сервиса стало включать `project.getPath()`
([native-build-tools#760](https://github.com/graalvm/native-build-tools/issues/760)).

**Ограничение:** ошибка возникает на конфигурации всего build'а, поэтому «собирать по одному
модулю» как обход не работает.

### 15.2. GraalVM 25 вместо 21

Меняется в трёх местах сразу — toolchain модуля, launcher самого native-образа и базовый образ Docker:

```groovy
kotlin {
    jvmToolchain(25)   // было 21
}

graalvmNative {
    binaries {
        main {
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(25)   // было 21
                vendor = JvmVendorSpec.matching("GraalVM Community")
            }
        }
    }
}
```

```dockerfile
FROM ghcr.io/graalvm/native-image-community:25 as builder   # было :21
```

**Причина:** артефакты Kora 2.0 собраны под JVM 25, а `native-image` не читает class-file формат
новее своего JDK. Отдельно проверьте, что `kotlin.jvmToolchain` и `jvmTarget` согласованы — рассогласование
даёт ошибку уже на этапе компиляции, а не сборки образа.

### 15.3. `imageName` и `mainClass`: передавать провайдер, а не интерполяцию

**Было** (Groovy DSL):

```groovy
imageName = "$project.name"
mainClass = "$application.mainClass"
```

**Стало:**

```groovy
imageName = project.name
mainClass = application.mainClass
```

В Kotlin DSL (`build.gradle.kts`) то же самое пишется явно через `set(...)`:

```kotlin
imageName.set(project.name)
mainClass.set(application.mainClass)
```

**Причина:** `application.mainClass` — это `Property<String>`. При строковой интерполяции в командную
строку `native-image` попадает не имя класса, а отладочное представление вида
`property(java.lang.String, fixed(...))`. Старый плагин разворачивал значение раньше и это работало
по совпадению; в 1.1.7 — нет.

**Поведенческое изменение:** проявляется как «main class not found» на этапе сборки образа, а не
как ошибка конфигурации Gradle.

### 15.4. `jar.enabled = false` больше нельзя

**Было:** `jar.enabled = false`, чтобы в `build/libs` оставался только shadow-артефакт.

**Стало:** строка удалена.

**Причина:** `nativeCompile` строит classpath из артефактов самого проекта. Без обычного `jar`
в classpath оказываются только зависимости, но не классы приложения (shadow-jar туда не входит —
он собирается для Docker-пути).

### 15.5. Метаданные достижимости: что теперь даёт сам фреймворк

Метаданные едут внутри артефактов Kora (`META-INF/native-image/<модуль>/`) и от языка приложения
не зависят. При миграции выяснилось, что пять наборов были неполными или не читались вовсе;
все пять исправлены в upstream:

| Симптом в native-образе | Модуль | PR |
|---|---|---|
| `java.lang.IllegalArgumentException: No XNIO provider found` на старте HTTP-сервера | `http-server-undertow` | [#811](https://github.com/kora-projects/kora/pull/811) |
| HikariCP не находит micrometer-трекер метрик | `database-jdbc` | [#812](https://github.com/kora-projects/kora/pull/812) |
| кеш Caffeine без статистики падает на создании | `cache-caffeine` | [#813](https://github.com/kora-projects/kora/pull/813) |
| регистрации молча не применяются | `micrometer-module`, `grpc-server`, `kafka` | [#814](https://github.com/kora-projects/kora/pull/814) |
| сборка образа падает на этапе анализа | `database-cassandra` | [#815](https://github.com/kora-projects/kora/pull/815) |

Самый переносимый вывод — четвёртая строка: файл с именем **`reflection-config.json`** native-image
не читает вовсе, правильное имя — **`reflect-config.json`**. Диагностики нет никакой: сборка идёт
успешно, просто регистрации никогда не применяются. В своём проекте это проверяется одной командой:

```shell
find . -name "reflection-config.json"   # каждое попадание — мёртвый файл
```

**Ограничение (важное):** не удаляйте собственные метаданные приложения на том основании,
что приложение работает на JVM. При переименовании пакетов каталог `META-INF/native-image/<group>/`
переименовывается вручную — ни OpenRewrite, ни скрипт каталоги ресурсов не трогают.

### 15.6. Как метаданные попадают в образ, собираемый в Docker

Два пути сборки ведут себя по-разному:

- `nativeCompile` сам подкладывает metadata repository (`graalvmNative.metadataRepository.enabled = true`);
- `native-image -cp application.jar` внутри Docker читает только то, что лежит в самом jar.

Чтобы второй путь видел те же метаданные, их собирают в ресурсы до упаковки:

```groovy
processResources.dependsOn tasks.collectReachabilityMetadata
sourceSets.main { resources.srcDirs += "$buildDir/native-reachability-metadata" }

shadowJar {
    mergeServiceFiles()   // без этого теряются META-INF/services — в native это фатально
}
```

Эта связка была в примерах и в 1.x — менять её при миграции не нужно, но её надо знать: если
образ из Gradle работает, а из Docker — нет, первый подозреваемый именно она, а не код.

### 15.7. Когда GraalVM-модуль считается мигрированным

Успешная сборка образа критерием не является: все пять дефектов из §15.5 давали зелёную сборку
и падали только в рантайме. Минимальный набор проверок:

1. бинарь стартует и не падает через секунду;
2. `GET /system/readiness` отвечает 200 — граф инициализировался целиком;
3. `GET /metrics` отдаёт метрики, а не заглушку и не 500;
4. сценарий модуля отрабатывает против реальной зависимости, а не на моках;
5. в логе нет стектрейсов при старте.

Ожидание готовности контейнера стройте на HTTP-пробе, а не на строке в логе: формулировки
стартовых сообщений Kora не являются контрактом и между 1.x и 2.0 поменялись:

```kotlin
waitingFor(
    Wait.forHttp("/system/readiness").forPort(8080).forStatusCode(200)
        .withStartupTimeout(Duration.ofSeconds(60))
)
```

### 15.8. Что специфично для Kotlin (гипотезы, не измерено)

Native-образ требует отдельной проверки Kotlin-приложения. Ниже — рекомендации,
которые нужно проверить на своём проекте, а не принимать на веру:

- **Вывод KSP дополнительных метаданных требовать не должен.** Сгенерированный `ApplicationGraph`
  — обычный JVM-байткод со статическими вызовами конструкторов; DI в Kora не рефлексивен
  ни в Java-, ни в Kotlin-варианте. Это главная причина ожидать паритета с Java.
- **`kotlin-reflect` — главный риск.** Он притягивается транзитивно (например, через Jackson
  `jackson-module-kotlin`) и требует своих регистраций. JSON в Kora 2.0 генерируется и в рефлексии
  не нуждается — перед native-сборкой стоит проверить `./gradlew <module>:dependencies` на предмет
  случайного `kotlin-reflect` и убрать его.
- **`data class` с аргументами по умолчанию** компилируется в дополнительный синтетический
  конструктор `<init>(..., int, DefaultConstructorMarker)`. Если какой-то библиотеке нужно создавать
  такой класс рефлексивно, регистрировать надо именно его, а не «видимый» конструктор из исходника.

Методика проверки любой из этих гипотез — трассирующий агент и минимальный native-пробник;
она описана в `KORA_2_MIGRATION_NEURO.md`, раздел
*Diagnostic pattern: native-image собрался, но падает в рантайме*.

### 15.9. GraalVM-модули на удалённой функциональности

Модуль на R2DBC или Vert.x не мигрируется в native не потому, что он native: сами интеграции удалены
из 2.0 (§12). Сначала переводите его на синхронный JDBC/Cassandra (§13), и только потом возвращайтесь
к native-части.

---

## 14. Что покрыто автоматизацией

| Изменение | OpenRewrite | Скрипт | Вручную / нейро-агент |
|---|---|---|---|
| Пакеты и координаты | только Java | ✅ | — |
| Переезд DI-аннотаций | только Java | ✅ | — |
| Переименования модулей | только Java | ✅ | — |
| Разделение HTTP-пакетов | — | ✅ | — |
| Kotlin/KSP pins | — | `--update-tooling` | toolchain/прочие плагины — вручную |
| `KspTask` → привязка по имени | — | REVIEW | проверить task API выбранного KSP |
| Режимы OpenAPI-генерации | — | — | ✅ |
| Nullability в Kotlin | — | REVIEW | ✅ |
| Типизированные resilient-спецификации | — | import-bound генерация | custom paths/коллизии — вручную |
| Снятие `suspend` по цепочке вызовов | — | — | ✅ |
| `@Component` на мапперы/интерцепторы | — | — | ✅ |
| S3-клиент | — | — | ✅ |
| Ключи конфигурации (`openapi.management.files`, `rapidoc`→`scalar`) | — | ✅ | — |
| Путь конфигурации openapi-клиента (`PetApi`→`petApi`) | — | — | ✅ |
| Нуллабельность в переопределениях (`result: T?`) | — | — | ✅ |
| `JdbcExecutor.SqlSupplier`/`SqlRunnable` в `inTx` | — | — | ✅ |
| `kspTest` в модулях с `@KoraApp` в тестах | — | — | ✅ |
| Именованные аргументы для сгенерированных TO | — | — | ✅ |
| GraalVM plugin, toolchain, Docker image | — | — | ✅ |
| `imageName`/`mainClass`, `jar.enabled` (§15.3–§15.4) | — | — | ✅ |
| Метаданные достижимости native-image | — | — | ✅ |

OpenRewrite в текущем виде **не трансформирует Kotlin** — для Kotlin-модулей рабочей автоматикой остаётся скрипт, а всё семантическое делается вручную или нейро-агентом по `KORA_2_MIGRATION_NEURO.md`.


## Актуальная целевая версия Kora 2.x

Сведения сверены с исходниками Kora в диапазоне `2.0.0.RC1..2.0.0.RC2`, затем
`2.0.0.RC2..5e9ef697b`. Документация: страницы Kora 2.x по выбранной интеграции.
Изменения после тега RC2 перечислены отдельно: они требуют сборки соответствующего HEAD,
а не опубликованного RC2. Все описанные ниже проверки входят в миграцию 1.x → 2.x.

### Версии и порядок

RC2 использует Kotlin `2.4.20`, KSP `2.3.12`, JUnit `6.1.3`, JDK 25.
Источник версий — `gradle/libs.versions.toml` в теге RC2. Это проверенная комбинация,
а не требование автоматически обновлять все сторонние зависимости проекта.

Для опубликованного релиза задайте `koraVersion=2.0.0.RC2`. Для локального HEAD используйте
версию, с которой выполнен `publishToMavenLocal` (обычно `2.0.0-SNAPSHOT`);
не выдавайте SNAPSHOT за содержимое релизного тега.

```shell
# Единый механический этап миграции 1.x → 2.x.
python "$MIGRATION_TOOLS/scripts/migrate_kora_2.py" --project-root "$PROJECT_ROOT"
python "$MIGRATION_TOOLS/scripts/migrate_kora_2.py" --project-root "$PROJECT_ROOT" --apply
# Дополнительные существующие этапы: сначала dry-run, затем --apply.
python "$MIGRATION_TOOLS/scripts/migrate_api_renames.py" --project-root "$PROJECT_ROOT"
python "$MIGRATION_TOOLS/scripts/migrate_config_keys.py" --project-root "$PROJECT_ROOT"
python "$MIGRATION_TOOLS/scripts/migrate_resilient_specs.py" --project-root "$PROJECT_ROOT" --help
# JDK 25 должен запускать сам Gradle.
./gradlew clean --continue
./gradlew classes testClasses --continue --no-build-cache
```

`migrate_kora_2.py` включает актуальные правила пакетов, версий и FQCN энкодера в Java/Kotlin/XML/JSON.
Проверьте version catalogs и нестандартные имена свойств вручную. SNAPSHOT не перезаписывается.
OpenRewrite: используйте существующий агрегат `io.koraframework.migration.Kora1To2`.

### Проверка используемых контрактов Kora 2.x

| Область / коммит | Изменение и действие |
|---|---|
| Logging `4873194f5` | `io.koraframework.logging.logback.ConsoleTextRecordEncoder` → `io.koraframework.logging.logback.text.ConsoleTextRecordEncoder`. Проверьте также `logback.xml`, native reflect-config и собственные реализации encoder SPI. Выбор фабрики через `kora.logging.encoder`; JSON требует `logging-logback-json`. Настройку XML и фабрики не заменяйте слепо. |
| Telemetry `72e6a1708`, `e6f9ea09c` | Появились глобальные `metrics.enabled` (по умолчанию true), `metrics.tags`, `MetricsTagsProvider`, `OpentelemetryTracingAttributesProvider`. Глобальный switch не включает метрики отдельных компонентов: их `telemetry.metrics.enabled` по умолчанию false. `telemetry.logging.enabled` — false, tracing — true. |
| Собственные telemetry-фабрики | Сверьте сигнатуры с RC2: `prometheusMeterRegistry` теперь принимает `MetricsConfig`; `opentelemetryResourceConfig` переименован в `opentelemetryTracingConfig`; `opentelemetryTracingResource` получает providers. Имена метрик, tags, spans, attributes изменены: обновите PromQL, alerts, dashboards и trace assertions по реальному scrape/trace. Глобальная замена строк здесь небезопасна. |
| HTTP `bf088040b` | Заголовок `Server` теперь выключен. Если клиенты или тесты требуют его, задайте `httpServer.headerServerNameEnabled=true` (для системного сервера настройка своя). |
| Kafka `a0a2b8c1d` | Assign consumer получает список из `config.topics()`; отдельный аргумент `String topic` конструктора удалён. При отсутствии `group.id` нужен непустой `topics`, `topicsPattern` запрещён. Для генерируемых listeners пересоздайте код, для ручных контейнеров исправьте constructor и config вместе. |
| Scheduling `2fba8ca77`, `85aec8a77` | JDK jobs используют virtual threads; `executionParallelism` ограничивает конкуренцию. Проверьте thread-local assumptions и лимиты внешних сервисов. Job config поддерживает `enabled`; отключённые jobs не должны ожидаться в тестах расписания. |
| DB scheduler `f24bfc2f3`, `b0e8cf54f`, `7b91c1520` | Новый `scheduling-db` — опциональный модуль. Ключ создания таблицы — `tableInitialize`, default false. Production schema переносите управляемой миграцией. Импорт модуля теперь действительно запускает scheduler. |
| Resilience `31e64c063`, `b788b5536` | Добавлены распределённые rate limiter/retry budget и исключения, не регистрируемые retry/circuit breaker. Старые custom predicates/specs проверьте вручную; локальные реализации не заменяйте распределёнными автоматически. |
| PostgreSQL `d03ce87fe` | `database-jdbc-postgres` — дополнительный модуль и мапперы PostgreSQL. Обычный JDBC не требует автоматической замены зависимости. |
| OpenAPI `60286239c`, `3edf979b8` | Добавлены `clientResponseMode=SUCCESSFUL`, диапазоны ответов, OpenID Connect/security fixes. Перегенерируйте Java/Kotlin; проверьте sealed response handling и типы исключений. Старые режимы не переключайте без изменения вызовов. |
| Native `7bb835d0b` | Обновлены reflection/resource metadata. Повторите native build с новыми артефактами; JVM compilation не проверяет native image. |
| DI / transactions | Исправлены `@Conditional`, nullable dependencies, subgraphs, refresh и JDBC post-commit/post-rollback (один вызов для своей транзакции). Удалите workaround только после теста соответствующего сценария. |

### Только после RC2: актуальный HEAD `5e9ef697b`

- `dd389c578`: `enableServerValidationInterceptor` теперь **false** по умолчанию.
  В RC2 при включённом `enableServerValidation` default был **true**. Для сохранения стандартного
  HTTP mapping ошибок явно задайте `enableServerValidationInterceptor="true"` при генерации HEAD.
- `693aca682`: form converters используют media-type tags; urlencoded object fields следуют OpenAPI form serialization.
  `urlEncodedFormObjectsAsJson="true"` возвращает legacy JSON в одном поле при отсутствии `encoding.contentType`.
  Проверьте wire format и пользовательские `HttpClientParameterWriter` / `HttpServerParameterReader`.
- `5e9ef697b`: удалён неработавший `upload.singlePartUploadLimit`; оставлены `upload.partSize` и `upload.chunkSize`.
  Удалите obsolete key вручную только в S3 upload config.
- OpenAPI исправляет nullable/required/enum/discriminator/anyOf/oneOf, JSON maps и form bodies.
  Перегенерируйте код, затем проверьте изменения публичных типов и контрактов; generated-файлы вручную не патчите.
- S3 исправляет URI с prefix/trailing slash, nullable get/head results, multipart и tagged factories.
  Проверьте реальные object keys, streaming uploads и собственные telemetry interceptors.

### Приёмка

Повторный запуск скрипта должен показать 0 изменений. Соберите production и test sources;
проверьте HTTP headers/errors/form payloads, Kafka assign без group, scheduler enable/disable,
transaction callbacks, `/metrics` и экспорт traces. Native и Docker integration проверяются отдельно.
Список исправлений релиза не означает, что каждый компонент нужно переписывать: меняйте код
только там, где проект использует затронутый контракт.
