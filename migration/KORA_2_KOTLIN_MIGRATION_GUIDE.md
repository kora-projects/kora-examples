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

// верно
val status = PetTO.StatusEnum.entries.first { it.value == raw }
```

Проявляется только на данных, компиляция чистая.

### 10.4 Теги требований безопасности нумеруются по порядку в спецификации

Экстракторы принципала подключаются по тегу `ApiSecurity.SecurityRequirementTagN`, где `N` —
**индекс требования в списке `security` спецификации**, а не имя схемы. Переименование схемы тег
не меняет; перестановка требований — меняет.

```kotlin
@Tag(ApiSecurity.SecurityRequirementTag0::class)
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

Артефакт `io.koraframework:test-junit5`, KSP-процессор `ksp("io.koraframework:symbol-processors")` также для test-конфигурации.

Пакет расширения: `io.koraframework.test.extension.junit5.*` — `@KoraAppTest`, `@TestComponent`, `KoraAppTestConfigModifier`, `KoraConfigModification`.

Тесты, написанные на `runTest` вокруг `suspend`-репозиториев, после перехода на синхронные контракты упрощаются до обычных тестов. MockK-моки `coEvery` заменяются на `every`.

**BOM нужно распространить и на `kspTest`.** Конфигурация тестового процессора не наследует
платформу автоматически, и версии артефактов Kora расходятся между `ksp` и `kspTest`:

```kotlin
val koraBom = configurations.create("koraBom")
dependencies { koraBom(platform("io.koraframework:kora-parent:$koraVersion")) }

configurations.ksp.get().extendsFrom(koraBom)
configurations.kspTest.get().extendsFrom(koraBom)
```

Симптом при пропуске: тестовый исходник обрабатывается процессором другой версии, и ошибки
выглядят как несуществующие методы сгенерированного кода.

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
