# Kora 1.x → 2.0: руководство по миграции Java-проектов

Самодостаточный документ: Java-разработчику для миграции своего проекта не нужно открывать Kotlin-гайд.
Правила, общие для двух языков, продублированы здесь целиком.

Источники истины: локальный чекаут фреймворка `../kora` @ `master` (`2.0.0-SNAPSHOT`), документация 1.x — `agents-md/kora-docs`,
эталонный мигрированный модуль — `examples/java/kora-java-crud`.

Каждое правило ниже либо проверено компиляцией/тестами, либо явно помечено как гипотеза.

---

## 0. Порядок миграции проекта

1. Прогнать OpenRewrite-рецепт (`migration/openrewrite/`) — пакеты, типы, координаты зависимостей.
2. Прогнать `python migration/scripts/migrate_kora_2.py` (сначала без флагов — dry-run, затем `--apply`) — то, что OpenRewrite не покрывает.
3. **Обязательно**: `clean` + первая сборка с `--no-build-cache` (см. §1.3).
4. Собрать модуль: `./gradlew <module>:classes --console=plain`.
5. Применить семантические правила из этого документа под конкретные ошибки компиляции.
6. Прогнать тесты модуля и только после этого переходить к следующему.

Оба слоя автоматизации идемпотентны. Перед запуском — закоммитить или сохранить рабочее дерево.

---

## 1. Сборка и окружение

### 1.1 JDK: Gradle-процесс должен идти на JDK 25

**Было:** Gradle на JDK 21, toolchain 21.

**Стало:** toolchain 25 **и** сам Gradle запущен на JDK 25.

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
```

**Причина:** артефакты Kora 2.0 собраны под JVM 25. Настройки toolchain недостаточно: `io.koraframework:openapi-generator` попадает в **buildscript classpath**, который резолвится JVM самого Gradle. На JDK 21 сборка падает ещё на конфигурации:

```
Dependency requires at least JVM runtime version 25. This build uses a Java 21 JVM.
> Run this build using a Java 25 or newer JVM.
```

**Проверка:** `JAVA_HOME=<JDK 25> ./gradlew projects` проходит; с JDK 21 — падает.

**Ограничение:** прописывать `org.gradle.java.home` в `gradle.properties` репозитория не стоит — путь машинозависим. Задавайте `JAVA_HOME` или используйте Gradle toolchain для запуска демона.

### 1.2 Плагины и координаты

| Было | Стало |
|---|---|
| `ru.tinkoff.kora` (groupId) | `io.koraframework` |
| BOM `ru.tinkoff.kora:kora-parent` | `io.koraframework:kora-parent` (имя BOM сохранилось) |
| `ru.tinkoff.kora:annotation-processors` | `io.koraframework:annotation-processors` |
| `json-module` | `json-common` |
| `cache-redis` | `cache-redis-lettuce` |
| `http-client-async` | **удалён**, замены нет — переходить на `http-client-jdk` или `http-client-ok` |
| `ru.tinkoff.kora.experimental:s3-client-aws` | `io.koraframework:s3-client-aws` (вышел из experimental) |

Эталонный набор зависимостей — `examples/java/kora-java-crud/build.gradle`:

```groovy
configurations {
    koraBom
    annotationProcessor.extendsFrom(koraBom); compileOnly.extendsFrom(koraBom); implementation.extendsFrom(koraBom)
    api.extendsFrom(koraBom); testImplementation.extendsFrom(koraBom); testAnnotationProcessor.extendsFrom(koraBom)
}

dependencies {
    koraBom platform("io.koraframework:kora-parent:$koraVersion")
    annotationProcessor "io.koraframework:annotation-processors"
    implementation "io.koraframework:http-server-undertow"
    testAnnotationProcessor "io.koraframework:annotation-processors"
    testImplementation "io.koraframework:test-junit5"
}
```

### 1.3 Первая сборка после смены пакетов — `clean` + `--no-build-cache`

**Симптом:** сотни ошибок вида `package ru.tinkoff.kora.common.annotation does not exist` в файлах, которых **нет** в исходниках — все пути ведут в `build/generated/…`.

**Причина:** задачи-генераторы (OpenAPI, protobuf, `wsdl2java`) не удаляют предыдущий вывод, а ключ build-кеша не учитывает смену `apiPackage`/`modelPackage`. В результате в один source set попадают старые файлы в пакете `ru.tinkoff.kora` и новые в `io.koraframework`.

**Решение:**

```shell
./gradlew clean --continue
./gradlew classes testClasses --continue --no-build-cache
```

**Проверка (выполнена):** на эталонном модуле `clean` + обычная сборка → 100 ошибок; та же сборка с `--no-build-cache` → успех. Подробности — `KORA_2_FRAMEWORK_ISSUES.md`.

---

## 2. Пакеты и аннотации

Все пакеты фреймворка: `ru.tinkoff.kora.*` → `io.koraframework.*`.

Аннотации DI переехали на уровень глубже — в `annotation`:

| Было | Стало |
|---|---|
| `ru.tinkoff.kora.common.KoraApp` | `io.koraframework.common.annotation.KoraApp` |
| `ru.tinkoff.kora.common.Component` | `io.koraframework.common.annotation.Component` |
| `ru.tinkoff.kora.common.DefaultComponent` | `io.koraframework.common.annotation.DefaultComponent` |
| `ru.tinkoff.kora.common.KoraSubmodule` | `io.koraframework.common.annotation.KoraSubmodule` |
| `ru.tinkoff.kora.common.Mapping` | `io.koraframework.common.annotation.Mapping` |
| `ru.tinkoff.kora.common.Module` | `io.koraframework.common.annotation.Module` |
| `ru.tinkoff.kora.common.Tag` | `io.koraframework.common.annotation.Tag` |
| `ru.tinkoff.kora.common.annotation.Root` | `io.koraframework.common.annotation.Root` |

Точка входа приложения (эталон):

```java
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;

@KoraApp
public interface Application extends HoconConfigModule, LogbackModule, UndertowPublicHttpServerModule {
    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
```

**Автоматизация:** полностью покрыто OpenRewrite-рецептом и скриптом.

---

## 3. Нуллабельность: JSpecify вместо jakarta

**Было:** `jakarta.annotation.Nullable`, `jakarta.annotation.Nonnull`.
**Стало:** `org.jspecify.annotations.Nullable` (JSpecify приходит транзитивно с ядром Kora). `@Nonnull` в большинстве мест избыточна и удаляется; если нужна разметка — предпочитайте `@NullMarked` на уровне пакета/модуля.

**Поведенческое отличие:** JSpecify-аннотации являются **type-use**. Их нельзя ставить там, где раньше стояла обычная аннотация объявления. Симптом:

```
error: type annotation @org.jspecify.annotations.Nullable is not expected here
```

Так же ведут себя и некоторые аннотации Kora: встречается `type annotation @io.koraframework.database.common.annotation.Column("status") is not expected here`.

**Правило:** в квалифицированных вложенных типах type-use аннотация ставится непосредственно перед простым именем типа (`Outer.@Nullable Inner`), а не перед всем выражением. Для массивов и дженериков позиция также значима.

**Автоматизация:** замена типа — да (OpenRewrite); корректная **расстановка** — нет, только вручную или нейро-агентом (см. `KORA_MIGRATION_NEURO.md`).

---

## 4. HTTP-сервер и HTTP-клиент

### 4.1 Модуль и синхронная модель

| Было | Стало |
|---|---|
| `UndertowHttpServerModule` | `UndertowPublicHttpServerModule` |

HTTP-обработка в 2.0 синхронная. Из контрактов контроллеров и интерцепторов уходят `CompletionStage`, реактивные обёртки и явный Kora `Context`.

`Context` **удалён** из синхронных HTTP API. Механически заменять его нечем: аргумент убирается, а сигнатура обработчика/интерцептора мигрирует целиком.

### 4.2 Пакеты HTTP-типов разделены

| Было | Стало |
|---|---|
| `http.server.common.HttpServerRequest` | `http.server.common.request.HttpServerRequest` |
| `http.server.common.HttpServerResponse` | `http.server.common.response.HttpServerResponse` |
| `http.server.common.HttpServerResponseException` | `http.server.common.response.HttpServerResponseException` |
| `http.server.common.HttpServerInterceptor` | `http.server.common.interceptor.HttpServerInterceptor` |
| `http.server.common.handler.HttpServerRequestMapper` | `http.server.common.request.HttpServerRequestMapper` |
| `http.server.common.handler.HttpServerResponseMapper` | `http.server.common.response.HttpServerResponseMapper` |
| `http.client.common.HttpClientResponseException` | `http.client.common.exception.HttpClientResponseException` |

### 4.3 Интерцептор: новая сигнатура

**Было (1.x):** интерцептор получал контекст и цепочку, возвращая `CompletionStage`.

**Стало:**

```java
@Tag(HttpServer.class)
@Component
public final class HttpExceptionHandler implements HttpServerInterceptor {

    @Override
    public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) {
        try {
            return chain.process(request);
        } catch (HttpServerResponseException e) {
            return e;
        } catch (Exception e) {
            var body = HttpBody.json(errorJsonWriter.toByteArray(new MessageTO(e.getMessage())));
            return HttpServerResponse.of(500, body);
        }
    }
}
```

**Тег глобального интерцептора сменился:**

| Было | Стало |
|---|---|
| `@Tag(HttpServerModule.class)` | `@Tag(HttpServer.class)` |
| `import …http.server.common.HttpServerModule;` | `import …http.server.common.HttpServer;` |

В 2.0 фреймворк собирает глобальные интерцепторы именно по этому тегу — в `HttpServerModule` параметр объявлен как `@Tag(HttpServer.class) All<HttpServerInterceptor> interceptors`.

**Коварство:** старый тег **компилируется без ошибок** — класс `HttpServerModule` существует, просто по нему никто не ищет интерцепторы. Глобальный интерцептор молча перестаёт вызываться: обработка ошибок, аутентификация или логирование исчезают без единого предупреждения. Проверяйте это тестом, а не компилятором.

**Проверка:** компилируется и работает в `examples/java/kora-java-crud`.

### 4.4 JSON-writer: `toByteArray` больше не бросает `IOException`

`toByteArrayUnchecked(value)` удалён — используйте `toByteArray(value)`.

Важное следствие: в 2.0 `toByteArray` **не объявляет checked-исключений** (`JsonWriter.toByteArray(@Nullable T)` без `throws`). Старый код с обработкой `IOException` перестаёт компилироваться:

```
error: exception IOException is never thrown in body of corresponding try statement
```

**Было:**

```java
try {
    return HttpServerResponse.of(code, HttpBody.json(errorJsonWriter.toByteArray(error)));
} catch (IOException ex) {
    return HttpServerResponse.of(500, HttpBody.plaintext(ex.getMessage()));
}
```

**Стало:**

```java
return HttpServerResponse.of(code, HttpBody.json(errorJsonWriter.toByteArray(error)));
```

Уберите также ставший ненужным `import java.io.IOException;`.

### 4.5 Мапперы и интерцепторы обязаны быть DI-компонентами

**Симптом:**

```
error: No component found for dependency:
  MapperRequestController.UserContextRequestMapper (no tags)
```

**Причина:** генерируемый модуль контроллера теперь **инжектит** маппер как зависимость, а не создаёт его сам.

**Было:**

```java
public static final class UserContextRequestMapper implements HttpServerRequestMapper<UserContext> { … }
```

**Стало:**

```java
@Component
public static final class UserContextRequestMapper implements HttpServerRequestMapper<UserContext> { … }
```

То же относится к классам из `@InterceptWith` и к маперам ключей кеша из `@Mapping`.

### 4.6 Реактивные контроллеры удалены

Метод контроллера, возвращающий `Mono<HttpServerResponse>`, в 2.0 неразрешим:

```
No component found for dependency: HttpServerResponseMapper<reactor.core.publisher.Mono<HttpServerResponse>>
```

Проверено по исходникам: в `http-server-common` нет ни одного маппера для `Mono`/`Flux`. Переводите такие методы на синхронный возврат `HttpServerResponse`; зависимость `io.projectreactor:reactor-core` после этого обычно становится ненужной.

**Диагностическая ловушка.** Пока в графе есть такой неразрешимый компонент, процессор может выводить ошибку про **другой, исправный** компонент — например, требование `JsonWriter<HttpResponseEntity<T>>` для `@Json`-метода, который на самом деле работает. Сначала уберите весь код, опирающийся на удалённую функциональность, и только потом анализируйте оставшиеся ошибки графа (`KORA_2_FRAMEWORK_ISSUES.md`).

### 4.7 HTTP-клиент: `configPath` → `value`

**Было:**

```java
@HttpClient(configPath = "httpClient.userApi")
public interface UserApiClient { … }
```

**Стало:**

```java
@HttpClient("httpClient.userApi")
public interface UserApiClient { … }
```

**Причина:** в 2.0 аннотация объявлена как `String value() default ""` плюс `telemetryTag()` и `httpClientTag()`; атрибута `configPath` больше нет.

**Симптом:** `error: cannot find symbol: method configPath() location: @interface HttpClient`.

**Автоматизация:** детерминированная замена `@HttpClient(configPath = "X")` → `@HttpClient("X")`, применима и к Java, и к Kotlin.

---

## 5. Конфигурация

### 5.1 Порты HTTP-сервера — молчаливый убийца старта

Системный (приватный) сервер получил собственную секцию `httpServer.system`:

| Было (1.x) | Стало (2.0) |
|---|---|
| `httpServer.publicApiHttpPort` | `httpServer.port` |
| `httpServer.privateApiHttpPort` | `httpServer.system.port` |
| `httpServer.privateApiHttpReadinessPath` | `httpServer.system.readinessPath` |
| `httpServer.privateApiHttpLivenessPath` | `httpServer.system.livenessPath` |
| `httpServer.privateApiHttpMetricsPath` | `httpServer.system.metricsPath` |

**Было:**

```hocon
httpServer {
  publicApiHttpPort = 8080
  privateApiHttpPort = 8085
}
```

**Стало:**

```hocon
httpServer {
  port = 8080
  system.port = 8085
}
```

**Почему это критично, а не косметика.** `SystemHttpServerConfig extends HttpServerConfig`, то есть системный сервер наследует `port()` со значением по умолчанию `8080`. Пока в конфиге стоит нераспознаваемый `privateApiHttpPort`, **оба** сервера пытаются сесть на 8080, и приложение падает на старте:

```
HTTP server 'kora-undertow-system' (Undertow) failed to start on port '8080': port is already in use
Caused by: java.net.BindException: Address already in use
```

Компиляция при этом проходит успешно — лишние ключи HOCON просто игнорируются. Ошибка всплывает только в рантайме, а в blackbox-тестах выглядит как `HTTP/1.1 header parser received no bytes` и таймауты, что уводит от причины.

**Автоматизация:** `python migration/scripts/migrate_config_keys.py --apply` (идемпотентен, есть dry-run).

### 5.2 Секция JDBC: `db` → `jdbc`

**Было:**

```hocon
db {
  jdbcUrl = ${POSTGRES_JDBC_URL}
  username = ${POSTGRES_USER}
  password = ${POSTGRES_PASS}
}
```

**Стало:**

```hocon
jdbc {
  jdbcUrl = ${POSTGRES_JDBC_URL}
  username = ${POSTGRES_USER}
  password = ${POSTGRES_PASS}
}
```

**Причина:** `JdbcDatabaseModule` в 2.0 создаёт `new JdbcDatabaseFactoryModule("jdbc")` — путь секции жёстко `jdbc`.

**Симптом:** компиляция зелёная, но приложение (и тесты) падают на старте:

```
ConfigValueException: Config expected value, but got null at path: 'ROOT.jdbc.username'
```

Ошибка вводит в заблуждение: говорится про `jdbc.username`, а в конфиге такого блока вообще нет — потому что секция всё ещё называется `db`.

**Автоматизация:** тот же `migrate_config_keys.py` (переименовывает только верхнеуровневой `db`-блок и только в файлах с `jdbcUrl`).

### 5.3 Остальное

| Было | Стало |
|---|---|
| `@ConfigValueExtractor` | `@ConfigMapper` |
| пакет `config.common.extractor` | удалён — типы переехали, ищите замену в `io.koraframework.config.common` |

`@ConfigSource` сохранил семантику. HOCON-подстановки (`${VAR}`, `${?VAR}`) не менялись.

---

## 6. Отказоустойчивость: строковые имена → типизированные спецификации

Самое объёмное семантическое изменение.

**Было:**

```java
@CircuitBreaker("pet")
@Retry("pet")
@Timeout("pet")
public Optional<PetWithCategory> findByID(long petId) { … }
```

**Стало:** аннотация принимает **класс** спецификации, а сама спецификация — интерфейс, привязанный к секции конфигурации:

```java
@CircuitBreakable(PetCircuitBreaker.class)
@Retryable(PetRetry.class)
@Timeout(PetTimeouter.class)
public Optional<PetWithCategory> findByID(long petId) { … }
```

```java
@CircuitBreakerSpec("resilient.circuitbreaker.pet")
public interface PetCircuitBreaker extends CircuitBreaker {}

@RetrySpec("resilient.retry.pet")
public interface PetRetry extends Retry {}

@TimeoutSpec("resilient.timeout.pet")
public interface PetTimeouter extends Timeouter {}
```

Соответствие аннотаций и пакетов:

| Было | Стало | Пакет |
|---|---|---|
| `@CircuitBreaker("name")` | `@CircuitBreakable(X.class)` | `io.koraframework.resilient.circuitbreaker.annotation` |
| `@Retry("name")` | `@Retryable(X.class)` | `io.koraframework.resilient.retry.annotation` |
| `@Timeout("name")` | `@Timeout(X.class)` | `io.koraframework.resilient.timeout.annotation` |
| `@Fallback(value = "name", method = "…")` | `@Fallback(method = "…")` | атрибут `value` удалён |

Изменения конфигурации:

```hocon
resilient {
  circuitbreaker.pet {
    type = FIXED_WINDOW          # обязателен для окна фиксированного размера
    countBased.windowSize = 50   # было slidingWindowSize
    minimumRequiredCalls = 25
    failureRateThreshold = 50
    waitDurationInOpenState = "25s"
    permittedCallsInHalfOpenState = 10
  }
  timeout.pet.duration = "5000ms"
  retry.pet { delay = "500ms"; delayStep = "5s"; attempts = 3 }
}
```

**Симптом незавершённой миграции:** `incompatible types: String cannot be converted to Class<? extends Timeouter>` (38 таких ошибок на момент написания).

**Автоматизация:** нет. Требуется завести новые типы и связать их с секциями конфигурации — это решение, которое скрипт принять не может.

---

## 7. Кеш

| Было | Стало |
|---|---|
| `@Cacheable(value = X.class, parameters = "id")` | `@Cacheable(value = X.class, args = "id")` |
| `@CacheInvalidate(value = X.class, invalidateAll = true)` | `@CacheInvalidateAll(X.class)` |
| `RedisCacheModule` (давал клиента) | `LettuceRedisCacheModule` из `cache-redis-lettuce` |

`RedisCacheModule` в 2.0 **существует** (`cache-redis-common`), но он транспортно-нейтрален и сам по себе не предоставляет `RedisCacheClient` — подключать нужно именно `LettuceRedisCacheModule`. Классы-мапперы ключей из `@Mapping` должны быть `@Component` (см. §4.5).

Эталон:

```java
@Cache("pet-cache")
public interface PetCache extends CaffeineCache<Long, PetWithCategory> {}
```

---

## 8. База данных

- `database.jdbc.EntityJdbc` → `database.jdbc.annotation.EntityJdbc`.
- Контракты репозиториев синхронные; макросы (`%{return#selects}`, `%{entity#inserts -= id}`, `%{entity#where = @id}`) сохранились.
- `@Id` на методе для генерируемого ключа, `UpdateCount` для числа изменённых строк — без изменений.

Эталон (`examples/java/kora-java-crud`):

```java
@Repository
public interface PetRepository extends JdbcRepository {

    @Query("SELECT p.id, p.name, p.status, p.category_id, c.name as category_name FROM pets p JOIN categories c on c.id = p.category_id WHERE p.id = :id")
    Optional<PetWithCategory> findById(long id);

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    long insert(Pet entity);

    @Query("DELETE FROM pets WHERE id = :id")
    UpdateCount deleteById(long id);
}
```

**Удалённые интеграции:** R2DBC и Vert.x (см. §12).

---

## 9. Планировщик

Quartz: `@ScheduleWithTrigger` принимает класс-тег напрямую.

**Было:** `@ScheduleWithTrigger(@Tag(MyJob.class))`
**Стало:** `@ScheduleWithTrigger(MyJob.class)`

---

## 10. OpenAPI-генерация

В Kora 2.0 остались только четыре режима: `java-client`, `java-server`, `kotlin-client`, `kotlin-server`.

| Было | Стало |
|---|---|
| `java-reactive-client` | `java-client` |
| `java-reactive-server` | `java-server` |

Смена режима необходима, но недостаточна: сгенерированный код нужно адаптировать к синхронным API. Известные проблемы на текущий момент: package-private интерфейсы API и рассогласование сигнатур делегатов (`method does not override or implement a method from a supertype`), `JsonNullable`. Разбирается в `KORA_2_FRAMEWORK_ISSUES.md`.

---

## 11. Тестирование

Пакет расширения: `io.koraframework.test.extension.junit5.*` (`@KoraAppTest`, `@TestComponent`, `KoraAppTestConfigModifier`, `KoraConfigModification`), артефакт `io.koraframework:test-junit5`.

Подход из эталона сохранился без изменений по смыслу: `@KoraAppTest(Application.class)`, моки через `@Mock @TestComponent`, конфиг — через `KoraConfigModification.ofString(...)`, интеграционные тесты — на Testcontainers.

Не забудьте `testAnnotationProcessor "io.koraframework:annotation-processors"`.

---

## 12. Удалённая и несовместимо изменённая функциональность

| Функциональность 1.x | Состояние в 2.0 | Что делать |
|---|---|---|
| R2DBC (`database-r2dbc`) | удалён | переводить на синхронный `database-jdbc` (Virtual Threads) либо оставлять как legacy-пример |
| Vert.x SQL (`database-vertx`) | удалён | то же |
| прочие интеграции Vert.x | удалены | `vertx-common` в дереве фреймворка ещё присутствует, но БД-интеграции нет |
| `http-client-async` | удалён | `http-client-jdk` / `http-client-ok` |
| реактивные контракты репозиториев | удалены | синхронные контракты |
| `@ConfigValueExtractor` | переименован | `@ConfigMapper` |
| `config.common.extractor` | пакет удалён | найти замену в `io.koraframework.config.common` |
| `Context` в синхронных HTTP API | удалён | переписать сигнатуру |
| `toByteArrayUnchecked` | удалён | `toByteArray` |
| `invalidateAll = true` | удалён | `@CacheInvalidateAll` |

Модули примеров, зависящие от удалённого, **не удалены**: они исключены из агрегатной сборки в `settings.gradle` и помечены `BLOCKED_BY_REMOVED_FUNCTIONALITY` в `KORA_2_MIGRATION_STATUS.md`. Решение об их переписывании или удалении — за владельцем репозитория.

---

## 13. Синхронная модель и Virtual Threads

Kora 2.0 строится на синхронных контрактах, исполняемых на виртуальных потоках. При миграции реактивного кода, который был реактивным только ради неблокирующего доступа к БД, следует переходить на синхронный эквивалент.

Это не механическое снятие обёрток: меняются отмена операции, границы транзакции и распространение исключений. Там, где требуется смысловая переработка, она документируется отдельно в `KORA_MIGRATION_NEURO.md`.

---

## 14. Что покрыто автоматизацией

| Изменение | OpenRewrite | Скрипт | Вручную / нейро-агент |
|---|---|---|---|
| Пакеты и координаты | ✅ | ✅ | — |
| Переезд DI-аннотаций в `…annotation` | ✅ | ✅ | — |
| Переименования модулей (`json-common`, `cache-redis-lettuce`) | ✅ | ✅ | — |
| Разделение HTTP-пакетов | — | ✅ | — |
| Версии плагинов, toolchain 25 | — | ✅ | — |
| Режимы OpenAPI-генерации | — | ✅ | адаптация кода — вручную |
| `jakarta` → JSpecify (тип) | ✅ | ✅ | **расстановка** — вручную |
| Типизированные resilient-спецификации | — | — | ✅ |
| `@Component` на мапперы/интерцепторы | — | частично (хардкод) | ✅ |
| Сигнатуры интерцепторов, удаление `Context` | — | — | ✅ |
| S3-клиент | — | — | ✅ |

Ограничения текущего скрипта `migrate_kora_2.py` описаны в `migration/README.md` — он содержит замены с хардкодом имён классов конкретных примеров и несколько слишком широких текстовых замен; для применения к чужому проекту требуется доработка.
