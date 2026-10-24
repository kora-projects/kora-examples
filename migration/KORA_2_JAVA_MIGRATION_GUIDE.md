# Kora 1.x → 2.x: руководство по миграции Java-проектов

Самодостаточный документ: Java-разработчику для миграции своего проекта не нужно открывать Kotlin-гайд.
Правила, общие для двух языков, продублированы здесь целиком.

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

1. Прогнать `python "$MIGRATION_TOOLS/scripts/migrate_kora_2.py" --project-root "$PROJECT_ROOT"` (сначала без флагов — dry-run, затем `--apply`) — пакеты, типы, координаты, версия, ресурсы. Перенос source paths — `--move-packages`.
2. Альтернатива для чисто Java-проектов, где текстовые замены нежелательны: OpenRewrite-рецепт `io.koraframework.migration.Kora1To2`, применяемый из самого мигрируемого проекта — `openrewrite/README.md`.
3. **Обязательно**: `clean` + первая сборка с `--no-build-cache` (см. §1.3).
4. Собрать модуль: `./gradlew <module>:classes --console=plain`.
5. Применить семантические правила из этого документа под конкретные ошибки компиляции.
6. Прогнать тесты модуля и только после этого переходить к следующему.

Оба слоя автоматизации идемпотентны. Перед запуском — закоммитить или сохранить рабочее дерево.

---

## 1. Сборка и окружение

### 1.1 JDK: JVM сборки и toolchain должны поддерживать целевой релиз

**Было:** Gradle на JDK 21, toolchain 21.

**Стало:** совместимый JDK для запуска Gradle и toolchain приложения. Baseline RC2 — JDK 25;
для более нового JDK проверьте совместимость wrapper/plugins и runtime deployment.

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25) // baseline RC2
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
```

**Причина:** артефакты Kora 2.0 собраны под JVM 25. Настройки toolchain недостаточно: `io.koraframework:openapi-generator` попадает в **buildscript classpath**, который резолвится JVM самого Gradle. На JDK 21 сборка падает ещё на конфигурации:

```
Dependency requires at least JVM runtime version 25. This build uses a Java 21 JVM.
> Run this build using a Java 25 or newer JVM.
```

**Проверка:** `JAVA_HOME=<compatible-JDK> ./gradlew projects` проходит; с JDK 21 — падает.

**Ограничение:** прописывать `org.gradle.java.home` в `gradle.properties` репозитория не стоит — путь машинозависим. Задавайте `JAVA_HOME` или используйте Gradle toolchain для запуска демона.

### 1.2 Плагины и координаты

| Было | Стало |
|---|---|
| `ru.tinkoff.kora` (groupId) | `io.koraframework` |
| BOM `ru.tinkoff.kora:kora-parent` | `io.koraframework:kora-bom` |
| `ru.tinkoff.kora:annotation-processors` | `io.koraframework:annotation-processors` |
| `json-module` | `json-common` |
| `cache-redis` | `cache-redis-lettuce` |
| `http-client-async` | **удалён**, замены нет — переходить на `http-client-jdk` или `http-client-ok` |
| `ru.tinkoff.kora.experimental:s3-client-aws` | см. §15 — S3 разделён на два разных артефакта, одного соответствия нет |

Пример зависимостей Gradle; адаптируйте к своей сборке:

```groovy
configurations {
    koraBom
    annotationProcessor.extendsFrom(koraBom); compileOnly.extendsFrom(koraBom); implementation.extendsFrom(koraBom)
    api.extendsFrom(koraBom); testImplementation.extendsFrom(koraBom); testAnnotationProcessor.extendsFrom(koraBom)
}

dependencies {
    koraBom platform("io.koraframework:kora-bom:$koraVersion")
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

**Проверка:** после clean перегенерируйте исходники без build cache и убедитесь, что старые пакеты исчезли из generated output.

---

### 1.4 Выравнивание сторонних версий — отдельный класс отказов

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

**Проверять после миграции**, а не полагаться на компиляцию: все три отказа рантаймовые, а
Byte Buddy к тому же прячется внутри `Application graph failed to initialize with N errors`
без видимых suppressed-исключений (Gradle их не печатает — временно включите `junitXml.required`).

### 1.5 Процессорам нужна полная перекомпиляция

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

Пример точки входа приложения:

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

**Автоматизация:** замена типа — да (OpenRewrite); корректная **расстановка** — нет, только вручную или нейро-агентом (см. `KORA_2_MIGRATION_NEURO.md`).

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

**Проверка в проекте:** соберите соответствующий source set и проверьте поведение компонента.

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

**Диагностическая ловушка.** Пока в графе есть такой неразрешимый компонент, процессор может выводить ошибку про **другой, исправный** компонент — например, требование `JsonWriter<HttpResponseEntity<T>>` для `@Json`-метода, который на самом деле работает. Сначала уберите весь код, опирающийся на удалённую функциональность, и только потом анализируйте оставшиеся ошибки графа (журнал подтверждённых проблем выбранного релиза).

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

### 4.6. `HttpResponseEntity<Void>` требует собственного маппера

Из коробки в 2.0 есть мапперы ответа только для `String` и `byte[]`. Метод, который отдаёт
`HttpResponseEntity<Void>` (обычный приём, когда от ответа нужен только статус-код), не
собирается:

```
error: No component found for dependency:
    HttpClientResponseMapper<java.lang.Void> (no tags)
```

Маппер объявляется в самом клиенте и **не** указывается через `@Mapping`:

```java
@Component
final class VoidResponseMapper implements HttpClientResponseMapper<Void> {

    @Override
    public Void apply(HttpClientResponse response) throws IOException {
        try (var body = response.body()) {
            body.asInputStream().readAllBytes();
        }
        return null;
    }
}
```

`@Mapping` здесь сделало бы только хуже: с ним маппер обязан произвести весь тип возврата,
то есть `HttpResponseEntity<Void>`, тогда как шаблонная фабрика фреймворка ждёт маппер
полезной нагрузки и оборачивает его в entity сама.

### 4.7. Мапперу нужен `@Component` ровно тогда, когда у него есть зависимости

Маппер без зависимостей Kora создаёт сама — пометить такой класс `@Component` значит получить
`Multiple components match`. Маппер, которому нужен, скажем, `JsonReader<T>`, она создать не
может, и без `@Component` будет `No component found for dependency` на его собственный тип.
Ориентироваться надо на конструктор, а не на вид аннотации над методом.

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

**Автоматизация:** `python "$MIGRATION_TOOLS/scripts/migrate_config_keys.py" --project-root "$PROJECT_ROOT" --apply` (идемпотентен, есть dry-run).

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

**Автоматизация:** только REVIEW. Скрипт сохраняет `db`: сравните выбранную factory и её config path, затем измените путь вручную при необходимости.

### 5.3 Остальное

| Было | Стало |
|---|---|
| `@ConfigValueExtractor` | `@ConfigMapper` |
| пакет `config.common.extractor` | удалён — типы переехали, ищите замену в `io.koraframework.config.common` |

`@ConfigSource` сохранил семантику. HOCON-подстановки (`${VAR}`, `${?VAR}`) не менялись.

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

**Автоматизация:** `migrate_resilient_specs.py` создаёт стандартные specs при явном Kora import. Общие интерфейсы, custom paths и конфликты согласуйте вручную.

---

### 6.1. Окно circuit breaker переехало в `countBased`

В 1.x у circuit breaker было одно счётное окно:

```hocon
resilient.circuitbreaker.pet {
  slidingWindowSize = 50
}
```

В 2.0 реализаций несколько (`STRIPED_APPROX` по умолчанию, `FIXED_WINDOW`, `RING_BUFFER`,
`TIME_BASED`), а окно переехало в отдельный блок:

```hocon
resilient.circuitbreaker.pet {
  type = FIXED_WINDOW
  countBased.windowSize = 50
}
```

Блок `countBased` в конфигурации формально `@Nullable`, но реализация по умолчанию
(`StripedApproxKoraCircuitBreaker`) разыменовывает `config.countBased()` без проверки, поэтому
на практике он обязателен, как только circuit breaker вообще используется. Без него — NPE
на инициализации графа, а не внятная ошибка конфигурации.

По смыслу ближе всего к счётному окну 1.x — `RING_BUFFER` (точная история последних N вызовов);
`FIXED_WINDOW` имеет другую семантику окна; выбирайте реализацию по требованиям приложения.
Скрипт `migrate_config_keys.py` переводит ключ в `FIXED_WINDOW` только при явном `--circuit-breaker-path`; если поведение
критично, тип стоит выбрать осознанно.

---

### 6.2. `CircuitBreakerPredicate` больше не выбирается по имени

В 1.x предикат отказа реализовывал `name()` и `test(Throwable)`, а circuit breaker находил его
по ключу конфигурации `failurePredicateName`.

В 2.0 это функциональный интерфейс с единственным методом, а связывание идёт через тег
спецификации:

```java
@Tag(DefaultCircuitBreaker.class)
@Component
public final class CircuitBreakerFailurePredicate implements CircuitBreakerPredicate {

    @Override
    public boolean isCircuitBreakerFailure(Throwable throwable) {
        return !(throwable instanceof HttpServerResponseException e) || e.code() >= 500;
    }
}
```

Ключ `failurePredicateName` из конфигурации удаляется — он больше ничего не выбирает.

### 6.3. `@Fallback` потерял имя

`FallbackSpec` в 2.0 нет, именованной конфигурации фолбэка тоже, поэтому атрибут `value`
удалён и остаётся только `method`:

```java
@Fallback(method = "createUserFallback(request)")
```

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

Пример:

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

Смена режима необходима, но недостаточна: сгенерированный код нужно адаптировать к синхронным API. Известные проблемы на текущий момент: package-private интерфейсы API и рассогласование сигнатур делегатов (`method does not override or implement a method from a supertype`), `JsonNullable`. Разбирается в журнал подтверждённых проблем выбранного релиза.

---

### 10.1. Сгенерированные enum: используйте `fromValue`

Wire-значение из OpenAPI (`available`) может не совпадать с именем Java-константы
(`AVAILABLE`). Не добавляйте ручной `statusOf`, поиск по `values()` или `Enum.valueOf`:

```java
var status = Pet.StatusEnum.fromValue(rawStatus);
```

`fromValue` генерируется Kora OpenAPI 2.0. Неизвестное значение приводит к
`IllegalArgumentException`; server delegate при необходимости преобразует его в
предусмотренный контрактом ответ `400`.

### 10.2. Security tags называются по security-схеме

Используйте вложенный тип, сгенерированный из имени в `components.securitySchemes`,
например `@Tag(ApiSecurity.ApiKeyAuth.class)` или
`@Tag(ApiSecurity.BearerAuth.class)`. Порядковые `SecurityRequirementTagN` и старые
варианты с маленькой буквы не соответствуют сгенерированному API 2.0.

---

## 11. Тестирование

Версию JUnit обновите до `6.1.3` через общую property:

```properties
junitVersion=6.1.3
```

Модули подключают `platform("org.junit:junit-bom:$junitVersion")`; версия
`org.testcontainers:junit-jupiter` относится к Testcontainers и не заменяется на версию JUnit.

Пакет расширения: `io.koraframework.test.extension.junit5.*` (`@KoraAppTest`, `@TestComponent`, `KoraAppTestConfigModifier`, `KoraConfigModification`), артефакт `io.koraframework:test-junit5`.

Базовая схема тестирования: `@KoraAppTest(Application.class)`, моки через `@Mock @TestComponent`, конфиг — через `KoraConfigModification.ofString(...)`, интеграционные тесты — на Testcontainers.

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

Найдите все модули на удалённых интеграциях, согласуйте замену с владельцем приложения и сохраните
их контракты/тестовые сценарии. Временное исключение модуля из сборки не завершает миграцию.

---

## 13. Синхронная модель и Virtual Threads

Kora 2.0 строится на синхронных контрактах, исполняемых на виртуальных потоках. При миграции реактивного кода, который был реактивным только ради неблокирующего доступа к БД, следует переходить на синхронный эквивалент.

Это не механическое снятие обёрток: меняются отмена операции, границы транзакции и распространение исключений. Там, где требуется смысловая переработка, она документируется отдельно в `KORA_2_MIGRATION_NEURO.md`.

---

## 15. S3: два разных артефакта вместо одного

В 2.0 под именем `s3-client-aws` существуют **два разных модуля**, и это легко перепутать:

| Артефакт | Что внутри |
|---|---|
| `io.koraframework:s3-client-aws` | только обёртка над AWS SDK: `AwsS3ClientModule`, конфиг, телеметрия. **Ни аннотаций `@S3`, ни моделей** |
| `io.koraframework.experimental:s3-client-kora` | декларативный клиент: `@S3` из `io.koraframework.s3.client.kora.annotation`, `S3Client`, фабрики |

Группа у всего, что лежит в `experimental/`, — `io.koraframework.experimental` (задаётся в `build.gradle` фреймворка). Туда же относятся `camunda-*`.

**Симптом неправильного артефакта:** `package S3 does not exist`, `package io.koraframework.s3.client.model does not exist`, `package io.koraframework.s3.client.annotation does not exist`.

### 15.1. `s3-client-minio` в 2.0 не существует

Реализация декларативного клиента поверх SDK Minio удалена (`experimental/s3-client-minio` остался
пустым каталогом и в `settings.gradle` фреймворка не подключён). Замена — `s3-client-kora`,
клиент поверх собственного HTTP-клиента Kora. Minio при этом прекрасно годится как S3-совместимое
хранилище для тестов — меняется артефакт, не тестовое окружение.

### 15.2. `s3-client-aws`: обёртка над SDK, а не декларативный клиент

Модуль отдаёт в контейнер сам `software.amazon.awssdk.services.s3.S3Client`, и работать с S3
предполагается через API AWS SDK:

```java
@Component
public class AwsS3Service {

    private final S3Client s3Client;

    public AwsS3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }
}
```

Бин выдаётся `AwsS3ClientFactoryModule` под `@Tag(Tag.Factory.class)`. Пугаться тега не нужно:
внутри `@FactoryModule` `@Tag.Factory` разворачивается в тег **самого метода фабричного модуля**
(`ComponentDeclaration.fromModule`), а `AwsS3ClientModule#awsS3ClientFactoryModule()` тега не имеет —
значит компонент нетегированный. Именно этот механизм позволяет объявить несколько фабричных модулей
с разными тегами и получить несколько независимо сконфигурированных клиентов.

Конфигурация переехала под `s3client.aws`, а ключи доступа — во вложенный объект:

```hocon
s3client.aws {
  url = ${S3_URL}
  credentials {
    accessKey = ${S3_ACCESS_KEY}
    secretKey = ${S3_SECRET_KEY}
  }
}
```

### 15.3. Новый API декларативного клиента

| 1.x | 2.0 |
|---|---|
| `io.koraframework.s3.client.annotation.S3` | `io.koraframework.s3.client.kora.annotation.S3` |
| `@S3.Get` для метаданных | `@S3.Head` → `HeadObjectResult` |
| `S3Object` | `GetObjectResult` (это `HttpClientResponse`, тело — `body().asInputStream()`) или `byte[]` |
| `S3ObjectMeta` | `HeadObjectResult` |
| `S3ObjectList` / `S3ObjectMetaList` | `ListBucketResult`, `List<String>`, `Iterator<ListBucketResult.ListBucketItem>` |
| `S3Body` | `byte[]`, `ByteBuffer`, `InputStream`, `S3Client.ContentWriter` |
| `S3ObjectUpload putObject(...)` | `String putObject(...)` (ETag) или `void` |
| `S3NotFoundException` | `io.koraframework.s3.client.kora.exception.S3ClientNoSuchKeyException` |
| `@S3.List(limit = 50)` | лимит задаётся через `ListObjectsArgs` |

Имя бакета больше не берётся из конфигурации клиента само — его указывают через `@S3.Bucket`:
путь с ведущей точкой (`@S3.Bucket(".bucket")`) отсчитывается от пути клиента из `@S3.Client`,
без точки — это абсолютный путь в конфигурации. Альтернатива — параметр метода под `@S3.Bucket`.

Конфигурация клиента: `endpoint` (было `url`), `credentials { accessKey, secretKey }`,
опционально `region`, `addressStyle`, `requestTimeout`, `upload`.

**Пакетного удаления в декларативном клиенте больше нет.** `@S3.Delete` генерирует только
`deleteObject`; метод вида `void deleteObjects(List<String> keys)` в 2.0 не поддерживается,
хотя `S3Client#deleteObjects` в runtime-API есть. Асинхронные и реактивные варианты клиента
убраны вместе с реактивной моделью.

---

### 15.4. Приложение, которому нужны и SDK, и декларативный клиент

Администрирование бакетов (создать, проверить существование) в декларативный контракт `@S3`
не входит. Приложению, которому нужно и то и другое, подключаются оба артефакта:

```groovy
implementation "io.koraframework:s3-client-aws"
implementation "io.koraframework.experimental:s3-client-kora"
```

```java
@KoraApp
public interface Application extends AwsS3ClientModule, KoraS3ClientModule, ... {}
```

Секции конфигурации у них разные и независимые: `s3client.aws` для SDK-обёртки и путь
из `@S3.Client(...)` для декларативного клиента.

Имя бакета из `@S3.Bucket` попадает в сгенерированный класс, а не в компонент, поэтому
код, которому оно нужно отдельно (тот же инициализатор бакета), читает тот же путь сам —
например через `@ConfigSource`.

Отдельная ловушка: компонент `Lifecycle`, который только готовит внешнее состояние и от
которого никто не зависит, выбрасывается из графа. Ему нужен `@Root`, иначе исчезнет и он,
и всё, что он тянул за собой:

```
interface software.amazon.awssdk.services.s3.S3Client wasn't found in graph
```

---

## 16. Camunda 8 / Zeebe

### 16.1. Клиент переименован

Camunda 8.8 переименовала клиент, и Kora 2.0 использует уже новый (`io.camunda:camunda-client-java`):

| 1.x | 2.0 |
|---|---|
| `io.camunda.zeebe.client.ZeebeClient` | `io.camunda.client.CamundaClient` |
| `io.camunda.zeebe.client.api.response.*` | `io.camunda.client.api.response.*` |
| `io.koraframework.camunda.zeebe.worker.JobWorkerException` | `io.koraframework.camunda.zeebe.worker.exception.JobWorkerException` |

`CamundaClient` инжектируется напрямую — `ZeebeWorkerModule` отдаёт его как `Wrapped<CamundaClient>`.

Отдельная засада в тестах: `io.camunda:zeebe-process-test-*` даже в версии 8.9.x всё ещё живёт на
старом `ZeebeClient`, и `BpmnAssert.assertThat(...)` принимает только его типы ответов. В тесте
поэтому остаётся старый клиент, тогда как само приложение работает на `CamundaClient` — это
нормально, объекты независимые.

### 16.2. REST-адрес стал обязательным

`ZeebeClientConfig#rest()` не помечен `@Nullable` и `RestConfig#url()` не имеет значения по умолчанию,
а `ZeebeWorkerModule` безусловно вызывает `clientConfig.rest().url()`. Конфигурация только с gRPC
теперь падает на старте:

```hocon
zeebe.client {
  grpc.url = ${ZEEBE_GRPC_URL}
  rest.url = ${ZEEBE_REST_URL}
}
```

### 16.3. Воркеры: два дефекта фреймворка, исправленные при миграции

Для выбранной версии проверьте следующие сценарии регрессий:

- `@Component`-воркер с `public`-методом под `@JobWorker` молча исчезал из графа
  (`No component found for dependency: X`, при том что `@Component` на `X` стоит);
- `JobWorkerException` перестал поднимать BPMN-ошибку — задача просто ретраилась.

Если минимальный пример подтверждает дефект именно выбранной версии, временные обходные пути: объявить метод-обработчик package-private (тогда класс
регистрируется) и не рассчитывать на boundary error event.

---

## 17. GraalVM Native Image

Для каждого native-модуля выполните отдельную сборку и запуск против используемых зависимостей.
Проверьте оба применимых пути: `nativeCompile` через Gradle и `native-image` внутри Docker.

Окружение, на котором всё проверено: GraalVM CE 25.2.4+7.1 (`native-image 25.0.4`), установка —
`sdk install java 25.2.4-graalce`. Образ для Docker-сборки — `ghcr.io/graalvm/native-image-community:25`.

### 17.1. Плагин `org.graalvm.buildtools.native`: 0.11.5 → 1.1.7

**Было:**

```groovy
id "org.graalvm.buildtools.native" version "0.11.5" apply false
```

**Стало:**

```groovy
id "org.graalvm.buildtools.native" version "1.1.7" apply false
```

**Причина:** на Gradle 9 задача `collectReachabilityMetadata` падает на резолве конфигурации
из чужого проекта. До 1.1.6 плагин регистрировал один shared build service `nativeConfigurationService`
на весь build — второй и последующие GraalVM-модули получали сервис, созданный в контексте
первого. В Gradle 8 это было предупреждением, в Gradle 9 — ошибка. В 1.1.6 имя сервиса стало
включать `project.getPath()` — сервис стал попроектным
([native-build-tools#760](https://github.com/graalvm/native-build-tools/issues/760)).

**Ограничение:** обходов в виде «запускать по одному модулю» недостаточно — ошибка возникает на
конфигурации всего build'а. Нужно поднимать версию плагина.

### 17.2. GraalVM 25 вместо 21

**Было / Стало** — три места, и менять нужно все три:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)   // было 21
        vendor = JvmVendorSpec.ADOPTIUM
    }
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

**Причина:** та же, что и в §1.1 — артефакты Kora 2.0 собраны под JVM 25. `native-image` отказывается
читать классы более нового class-file формата, чем его собственный JDK.

**Проверка:** `native-image --version` внутри выбранного launcher'а должен показать 25.x.
Если в системе нет GraalVM, `nativeCompile` падает на поиске toolchain — это не ошибка миграции.

### 17.3. `imageName` и `mainClass`: передавать провайдер, а не интерполяцию

**Было:**

```groovy
imageName = "$project.name"
mainClass = "$application.mainClass"
```

**Стало:**

```groovy
imageName = project.name
mainClass = application.mainClass
```

**Причина:** `application.mainClass` — это `Property<String>`, а не `String`. В строковой интерполяции
Groovy вызывает `toString()` самого свойства, и в командную строку `native-image` уезжает не имя класса,
а отладочное представление вида `property(java.lang.String, fixed(...))`. На старом плагине это
работало по совпадению (значение разворачивалось раньше), в 1.1.7 — нет.

**Поведенческое изменение:** ошибка выглядит как «main class not found» или как бинарь со странным
именем — не как ошибка конфигурации Gradle.

### 17.4. `jar.enabled = false` больше нельзя

**Было:** модули гасили обычный `jar`, чтобы в `build/libs` лежал только shadow-артефакт:

```groovy
jar.enabled = false
```

**Стало:** строка удалена.

**Причина:** `nativeCompile` строит classpath из артефактов самого проекта. С выключенным `jar`
в classpath попадают только зависимости, а классы приложения — нет. Shadow-jar здесь не спасает:
он собирается для Docker-пути и в classpath `nativeCompile` не участвует.

**Проверка:** если строку вернуть, сборка падает на том, что не находит класс `Application`,
хотя `compileJava` прошёл.

### 17.5. Метаданные достижимости: что теперь даёт сам фреймворк

Kora поставляет метаданные внутри своих артефактов
(`META-INF/native-image/<модуль>/`). При миграции выяснилось, что пять наборов были неполными
или не читались вовсе. Все пять исправлены в upstream:

| Симптом в native-образе | Модуль | PR |
|---|---|---|
| `java.lang.IllegalArgumentException: No XNIO provider found` на старте HTTP-сервера | `http-server-undertow` | [#811](https://github.com/kora-projects/kora/pull/811) |
| HikariCP не находит micrometer-трекер метрик | `database-jdbc` | [#812](https://github.com/kora-projects/kora/pull/812) |
| кеш Caffeine без статистики падает на создании | `cache-caffeine` | [#813](https://github.com/kora-projects/kora/pull/813) |
| регистрации молча не применяются | `micrometer-module`, `grpc-server`, `kafka` | [#814](https://github.com/kora-projects/kora/pull/814) |
| сборка образа падает на этапе анализа | `database-cassandra` | [#815](https://github.com/kora-projects/kora/pull/815) |

Самый переносимый вывод — четвёртая строка: файл с именем **`reflection-config.json`** native-image
не читает вовсе. Правильное имя — **`reflect-config.json`**. Ошибка не диагностируется никак:
сборка идёт успешно, просто регистрации не применяются. Проверьте имена файлов в своём проекте
отдельно — это одна команда:

```shell
find . -name "reflection-config.json"   # каждое попадание — мёртвый файл
```

**Ограничение (важное):** не удаляйте собственные метаданные приложения на том основании,
что приложение работает на JVM. В примерах собственные метаданные остались только для logback
(`src/main/resources/META-INF/native-image/com.acme/application/`) — они нужны и сейчас.
При переименовании пакетов каталог с именем группы (`.../native-image/<group>/`) переименовывается
вручную — ни OpenRewrite, ни скрипт каталоги ресурсов не трогают.

### 17.6. Как метаданные попадают в образ, собираемый в Docker

Два пути сборки ведут себя по-разному, и это источник путаницы:

- `nativeCompile` сам подкладывает metadata repository (`graalvmNative.metadataRepository.enabled = true`);
- `native-image -cp application.jar` внутри Docker никакого Gradle не видит и читает только то,
  что лежит в самом jar.

Чтобы второй путь видел те же метаданные, их надо собрать в ресурсы до упаковки:

```groovy
processResources.dependsOn tasks.collectReachabilityMetadata
sourceSets.main { resources.srcDirs += "$buildDir/native-reachability-metadata" }

shadowJar {
    mergeServiceFiles()   // без этого теряются META-INF/services — в native это фатально
}
```

Эта связка была в примерах и в 1.x — менять её при миграции не нужно, но её надо знать: если
образ из Gradle работает, а из Docker — нет, первый подозреваемый именно она, а не код.

### 17.7. Когда GraalVM-модуль считается мигрированным

Сборка образа — не критерий: все перечисленные в §17.5 дефекты давали **успешную сборку**
и падали только в рантайме. Минимальный набор проверок после сборки:

1. бинарь стартует и не падает через секунду;
2. `GET /system/readiness` отвечает 200 — значит граф инициализировался целиком;
3. `GET /metrics` отдаёт метрики, а не заглушку и не 500 (см. #810 и #814);
4. сценарий модуля отрабатывает против реальной зависимости (БД, брокер), а не на моках;
5. в логе нет стектрейсов при старте — они тут часто единственный признак отвалившейся подсистемы.

Самый дешёвый способ закрепить это в CI — `BlackBoxTests`, которые собирают образ по `Dockerfile`
и гоняют его Testcontainers'ами против зависимостей вашего приложения.

**Отдельно про ожидание готовности контейнера.** Не ждите строку в логе — формулировки
стартовых сообщений Kora не являются контрактом и между 1.x и 2.0 поменялись. Ждите HTTP-пробу:

```java
waitingFor(Wait.forHttp("/system/readiness").forPort(8080).forStatusCode(200)
        .withStartupTimeout(Duration.ofSeconds(60)));
```

### 17.8. GraalVM-модули на удалённой функциональности

Модули на R2DBC и Vert.x требуют замены удалённых интеграций (§12), включая DI wiring,
транзакции, backpressure и tests. Их исключение из сборки — временный шаг, а не завершённая миграция. Если у вас такой модуль — сначала переводите его на синхронный JDBC/Cassandra
(§13), и только потом возвращайтесь к native-части.

### 17.9. Диагностика

Отладка native-образа плохо формализуется в правила «было → стало»: одна и та же ошибка может
быть и дефектом фреймворка, и нехваткой метаданных приложения. Рабочая методика — трассирующий
агент, изоляция через минимальный native-пробник и проверка того, что метаданные вообще читаются —
описана в `KORA_2_MIGRATION_NEURO.md`, раздел *Diagnostic pattern: native-image собрался, но падает в рантайме*.

---

## 14. Что покрыто автоматизацией

| Изменение | OpenRewrite | Скрипт | Вручную / нейро-агент |
|---|---|---|---|
| Пакеты и координаты | ✅ | ✅ | — |
| Переезд DI-аннотаций в `…annotation` | ✅ | ✅ | — |
| Переименования модулей (`json-common`, `cache-redis-lettuce`) | ✅ | ✅ | — |
| Разделение HTTP-пакетов | — | ✅ | — |
| Kotlin/KSP pins | — | `--update-tooling` | toolchain/прочие плагины — вручную |
| Режимы OpenAPI-генерации | — | — | ✅ |
| `jakarta` → JSpecify (тип) | ✅ | REVIEW | **расстановка** — вручную |
| Типизированные resilient-спецификации | — | import-bound генерация | custom paths/коллизии — вручную |
| `@Component` на мапперы/интерцепторы | — | — | ✅ |
| Сигнатуры интерцепторов, удаление `Context` | — | — | ✅ |
| S3-клиент | — | — | ✅ |
| GraalVM plugin, toolchain, Docker image | — | — | ✅ |
| `imageName`/`mainClass`, `jar.enabled` (§17.3–§17.4) | — | — | ✅ |
| Метаданные достижимости native-image | — | — | ✅ |

Область и границы скриптов описаны в начале руководства и в `README.md`. Пропущенные `REVIEW`-случаи требуют семантической миграции; имена классов приложения не зафиксированы.
## Java Structured Concurrency для мигрируемого Kotlin-кода

Если Kotlin-часть приложения использовала Structured Coroutines Concurrency
(`coroutineScope`, `supervisorScope`, `async`/`await`, `awaitAll`, structured cancellation или
timeouts), выберите синхронное выполнение, virtual threads или Java `StructuredTaskScope` после анализа cancellation/timeout semantics. Последний использует preview API и не является обязательным условием миграции Kora.

Если выбран `StructuredTaskScope`, используйте API preview именно закреплённого JDK, а не
шаблон от другой версии. Проверьте совместимость Kotlin/compiler/plugins и release notes этого JDK.

Preview включается для всех фаз: `JavaCompile` — `--enable-preview` плюс совпадающий `--release`,
Kotlin compilation — `-Xjdk-release=<selected-JDK-major>` и `-Xjvm-enable-preview`, tests/`JavaExec`/
production launcher — `--enable-preview`. Build image и runtime image обязаны иметь одинаковый
major JDK. Выбор `Joiner`, timeout, failure/cancellation и supervisor semantics является
семантической миграцией и проверяется отдельными тестами.

Полный Kotlin/Gradle шаблон и checklist находятся в Kotlin guide, §13.

---


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
