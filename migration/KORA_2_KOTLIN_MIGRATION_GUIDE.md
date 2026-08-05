# Kora 1.x → 2.0: руководство по миграции Kotlin-проектов

Самодостаточный документ: Kotlin-разработчику для миграции своего проекта не нужно открывать Java-гайд.
Правила, общие для двух языков, продублированы здесь целиком, с идиоматичными Kotlin-примерами.

Источники истины: локальный чекаут фреймворка `../kora` @ `master` (`2.0.0-SNAPSHOT`), документация 1.x — `agents-md/kora-docs`.
Эталонный полностью мигрированный Kotlin-модуль на момент написания отсутствует — эталон существует только для Java
(`examples/java/kora-java-crud`), поэтому Kotlin-специфичные правила помечаются как проверенные только там, где это подтверждено сборкой.

---

## 0. Порядок миграции проекта

1. OpenRewrite-рецепт (`migration/openrewrite/`) — **только для Java-исходников и Gradle**; Kotlin-код он не трансформирует.
2. `python migration/scripts/migrate_kora_2.py` (сначала dry-run, затем `--apply`) — Kotlin-файлы, Gradle Kotlin DSL, ресурсы, перенос пакетных каталогов.
3. **Обязательно**: `clean` + первая сборка с `--no-build-cache` (см. §1.4).
4. `./gradlew <module>:kspKotlin --console=plain` — KSP отрабатывает раньше компиляции и даёт первую волну ошибок.
5. Применить семантические правила из этого документа.
6. Прогнать тесты модуля и только затем переходить к следующему.

---

## 1. Сборка и окружение

### 1.1 JDK: Gradle-процесс должен идти на JDK 25

```kotlin
kotlin {
    jvmToolchain(25)
}
```

Toolchain недостаточно: `io.koraframework:openapi-generator` попадает в **buildscript classpath**, который резолвится JVM самого Gradle. На JDK 21 конфигурация падает:

```
Dependency requires at least JVM runtime version 25. This build uses a Java 21 JVM.
```

**Проверка:** `JAVA_HOME=<JDK 25> ./gradlew projects` проходит, на JDK 21 — нет.

### 1.2 Kotlin и KSP

| Было | Стало |
|---|---|
| Kotlin `1.9.25` | `2.4.10` |
| KSP `1.9.25-1.0.20` | `2.3.11` |
| `kapt` `1.9.25` | `2.4.10` (там, где kapt ещё нужен — например, MapStruct) |

Эти версии совпадают с теми, на которых собран сам фреймворк (`../kora/gradle/libs.versions.toml`) — расхождение версий приведёт к трудноотлаживаемым падениям процессоров.

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
| BOM `ru.tinkoff.kora:kora-parent` | `io.koraframework:kora-parent` |
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

**Проверка (выполнена на Java-эталоне):** обычная сборка после `clean` → 100 ошибок; с `--no-build-cache` → успех.

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

**Автоматизация:** удаление импортов и простых форм `@Nullable` покрыто скриптом; случаи, где аннотация несла смысл (например, платформенные типы на границе с Java), требуют ручного решения.

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

**Диагностическая ловушка:** пока в графе остаётся неразрешимый компонент, сообщение об ошибке может указывать на другой, исправный компонент. Сначала убирайте код на удалённой функциональности, потом разбирайте остаток.

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

**Автоматизация:** `python migration/scripts/migrate_config_keys.py --apply`.

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

**Автоматизация:** `migrate_config_keys.py`.

### 5.3 Остальное

| Было | Стало |
|---|---|
| `@ConfigValueExtractor` | `@ConfigMapper` |
| пакет `config.common.extractor` | удалён |

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

**Важно для Kotlin:** пока строковая форма остаётся в коде, KSP-процессор resilient падает с
`java.lang.ClassCastException: java.lang.String cannot be cast to com.google.devtools.ksp.symbol.KSType`
вместо внятной диагностики. Это подтверждённая связка «немигрированный код → краш процессора» (см. `KORA_2_FRAMEWORK_ISSUES.md`).

**Автоматизация:** нет — требуется завести новые типы и связать их с секциями конфигурации.

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

Сохранять `suspend` «ради стиля» поверх синхронного контракта не следует. Там, где корутины остаются в собственном коде приложения (вне контрактов Kora), мост возможен, но должен быть осознанным решением.

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

---

## 11. Тестирование

Артефакт `io.koraframework:test-junit5`, KSP-процессор `ksp("io.koraframework:symbol-processors")` также для test-конфигурации.

Пакет расширения: `io.koraframework.test.extension.junit5.*` — `@KoraAppTest`, `@TestComponent`, `KoraAppTestConfigModifier`, `KoraConfigModification`.

Тесты, написанные на `runTest` вокруг `suspend`-репозиториев, после перехода на синхронные контракты упрощаются до обычных тестов. MockK-моки `coEvery` заменяются на `every`.

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

## 13. Синхронная модель и Virtual Threads

Kora 2.0 исполняет синхронные контракты на виртуальных потоках. Kotlin-код, который был `suspend` только ради неблокирующего доступа к БД или HTTP, переводится в синхронный. Корутины остаются допустимы во внутренней логике приложения, но не в контрактах фреймворка.

Изменения, которые нужно продумать при снятии `suspend`: отмена, границы транзакций, распространение исключений, необходимость `Dispatchers.IO` (не нужен — виртуальные потоки), тесты.

---

## 14. Что покрыто автоматизацией

| Изменение | OpenRewrite | Скрипт | Вручную / нейро-агент |
|---|---|---|---|
| Пакеты и координаты | только Java | ✅ | — |
| Переезд DI-аннотаций | только Java | ✅ | — |
| Переименования модулей | только Java | ✅ | — |
| Разделение HTTP-пакетов | — | ✅ | — |
| Версии Kotlin/KSP, toolchain 25 | — | ✅ | — |
| `KspTask` → привязка по имени | — | ✅ | проверить каждое место |
| Режимы OpenAPI-генерации | — | ✅ | адаптация кода — вручную |
| Удаление `@Nullable` в Kotlin | — | ✅ | спорные случаи — вручную |
| Типизированные resilient-спецификации | — | — | ✅ |
| Снятие `suspend` по цепочке вызовов | — | — | ✅ |
| `@Component` на мапперы/интерцепторы | — | частично (хардкод) | ✅ |
| S3-клиент | — | — | ✅ |

OpenRewrite в текущем виде **не трансформирует Kotlin** — для Kotlin-модулей рабочей автоматикой остаётся скрипт, а всё семантическое делается вручную или нейро-агентом по `KORA_MIGRATION_NEURO.md`.
