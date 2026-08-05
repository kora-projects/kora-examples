# Kora 2.0 — журнал дефектов, найденных при миграции

Здесь фиксируется всё подозрительное, что может оказаться проблемой Kora 2.0, включая неподтверждённые гипотезы.
Гипотезы помечены явно и уточняются по мере расследования. Запись здесь **не** означает, что баг подтверждён.

Источник истины по фреймворку — локальный чекаут `../kora` @ `master` (`io.koraframework:*:2.0.0-SNAPSHOT`, Maven Local).

---

## Issue: JSON-фабрика `httpClientResponseJsonEntityResponseMapper` не помечена `@Json`

- Status: **Fixed** (локально, готово к PR)
- Severity: Major
- Type: Framework bug
- Language: Java, Kotlin (общий runtime-модуль)
- Runtime: JVM
- Component: `http-client-common`, разрешение зависимостей `@KoraApp`
- Affected framework module: `http/http-client-common`
- Affected example modules: `examples/java/kora-java-http-client` (и любой клиент с `HttpResponseEntity<T>` в приложении с JSON)
- Framework commit: база `66800169f`, фикс `3ef4560af`
- Related fix branch: `fix/http-client-json-response-entity-mapper-tag` (локальная, **не отправлена**)
- Related PR: не создавался

### Description

Любой метод HTTP-клиента, возвращающий `HttpResponseEntity<T>`, не собирается в приложении, где в графе есть `JsonReader<T>`.

### Actual behavior

```
error: Multiple components match dependency:
    HttpClientResponseMapper<HttpResponseEntity<String>> (no tags)
  Candidates:
    - factory HttpClientResponseMapperModule#httpClientResponseEntityResponseMapper(...)
    - factory HttpClientResponseMapperModule#httpClientResponseJsonEntityResponseMapper(...)
```

### Suspected cause → подтверждённая причина

В `HttpClientResponseMapperModule` все JSON-варианты помечены `@Json` (`httpClientJsonEitherResponseMapper`, `httpClientJsonEitherResponseEntityResponseMapper`), кроме `httpClientResponseJsonEntityResponseMapper(JsonReader<T>)`. Без тега две `@DefaultComponent`-шаблонные фабрики дают один и тот же тип без тегов, и граф не может выбрать.

### Implemented fix

Добавлена аннотация `@Json` на `httpClientResponseJsonEntityResponseMapper`.

### Test coverage

`HttpClientExtensionTest#testExtensionResponseEntityWhenJsonReaderIsPresent` — проверено, что тест **падает без фикса** (`Multiple components match dependency`) и проходит с ним. Существовавший `testExtensionWithoutTag` дефект не ловил: в его графе нет `JsonReader`.

### Compatibility impact

Код, который полагался на неявный JSON-маппинг `HttpResponseEntity<T>` без `@Json`, теперь должен указывать `@Json` явно. Раньше такой код в любом случае не собирался.

### Validation

`:http:http-client-common:test`, `:http:http-client-annotation-processor:test`, `:http:http-client-symbol-processor:test` — зелёные; `examples/java/kora-java-http-client` — компилируется, все 9 тестов проходят.

---

## Issue: KSP-процессоры Kora 2.0 падают с внутренним исключением вместо диагностики

- Status: Investigating
- Severity: Blocker
- Type: Framework bug
- Language: Kotlin
- Runtime: JVM
- Component: KSP symbol processors (`symbol-processors`, `kora-app-symbol-processor`, модульные процессоры)
- Affected framework module: уточняется по каждому подвиду
- Affected example modules: 22 модуля со статусом `BLOCKED_BY_FRAMEWORK_BUG` в `KORA_2_MIGRATION_STATUS.md`
- Framework commit: `66800169f` (master на момент прогона)
- Related migration guide: `KORA_2_KOTLIN_MIGRATION_GUIDE.md`
- Related fix branch / PR: пока нет

### Description

При компиляции Kotlin-модулей задача `kspKotlin` завершается не сообщением об ошибке в коде, а необработанным исключением процессора. Пользователь не получает ни файла, ни строки, ни имени символа — только стектрейс-заголовок.

Наблюдаются как минимум шесть различных подвидов:

| Исключение | Пример модуля |
|---|---|
| `java.util.NoSuchElementException: No TypeParameter found for index T` | `examples/kotlin/kora-kotlin-helloworld`, `guides/kotlin/kora-kotlin-guide-cache-app` и ещё ~8 |
| `ksp.org.jetbrains.kotlin.analysis.api.lifetime.KaInvalidLifetimeOwnerAccessException` | `examples/kotlin/kora-kotlin-crud`, оба `kora-kotlin-crud-submodule-*-api` |
| `tools.jackson.core.exc.JacksonIOException: Stream closed` | `examples/kotlin/kora-kotlin-camunda-engine`, `guides/kotlin/kora-kotlin-guide-database-cassandra-app` |
| `java.lang.NullPointerException` | `examples/kotlin/kora-kotlin-grpc-server`, оба `guide-grpc-server*` |
| `java.lang.IllegalStateException: Required value was null` | `examples/kotlin/kora-kotlin-kafka` |
| `java.lang.ClassCastException: java.lang.String cannot be cast to com.google.devtools.ksp.symbol.KSType` | `examples/kotlin/kora-kotlin-resilient` |

### Steps to reproduce

```shell
export JAVA_HOME=<JDK 25>
./gradlew :examples:kotlin:kora-kotlin-helloworld:kspKotlin --no-build-cache --stacktrace
```

### Expected behavior

Процессор либо обрабатывает конструкцию, либо сообщает внятную ошибку с файлом, строкой и символом.

### Actual behavior

`e: [ksp] <исключение>` без указания места в коде; задача падает.

### Investigation notes

- Версии совпадают с теми, на которых собран сам фреймворк: Kotlin `2.4.10`, KSP `2.3.11` (`../kora/gradle/libs.versions.toml`). Рассинхрон версий как причина исключён.
- **Подтверждено, что минимум один подвид — реакция на немигрированный код, а не самостоятельный баг.** `ClassCastException: String → KSType` в `kora-kotlin-resilient` возникает потому, что код всё ещё использует строковый API `@CircuitBreaker("pet")`, тогда как в 2.0 атрибут имеет тип класса. То есть первопричина — код примера; дефектом фреймворка здесь является **отсутствие диагностики** (падение вместо сообщения «ожидался класс, получена строка»).
- Гипотеза (не проверена): часть подвидов `NoSuchElementException: No TypeParameter found` также вызвана несоответствием кода новому API и исчезнет после семантической миграции модуля. Проверяется по мере миграции — каждый модуль перепроверяется после правок, и запись уточняется.

### Suspected cause

Процессоры читают атрибуты аннотаций и параметры типов без валидации формы входных данных, полагаясь на структуру кода 2.0.

### Workaround

Мигрировать код модуля на API 2.0; для диагностики запускать `kspKotlin` отдельно с `--stacktrace`.

### Proposed fix

Обернуть чтение атрибутов аннотаций и разрешение параметров типов в проверки с выдачей `KSPLogger.error(...)`, указывающей символ. Отдельно — разобрать каждый подвид, который воспроизводится на корректном коде 2.0.

### Resolution

Не закрыт.

---

## Issue: неразрешимая зависимость в одном месте графа даёт сообщение про совсем другой компонент

- Status: Investigating (первоначальный вывод об ошибке выбора фабрики **опровергнут** экспериментом, см. Investigation notes)
- Severity: Minor — стоимость в потерянном времени на отладку, не в поведении
- Type: Framework bug (диагностика)
- Language: Java (Kotlin не проверялся)
- Runtime: JVM
- Component: разрешение зависимостей `@KoraApp` / `http-server-common`
- Affected framework module: `http/http-server-common`, `kora-app-annotation-processor`
- Affected example modules: `examples/java/kora-java-http-server`
- Framework commit: `66800169f`
- Related migration guide: `KORA_2_JAVA_MIGRATION_GUIDE.md`
- Related fix branch / PR: пока нет

### Description

Когда в графе есть компонент с принципиально неразрешимой зависимостью, сообщение об ошибке может указывать на совсем другой, исправный компонент — тот, чей шаблонный кандидат пробовался при переборе. На реальном модуле это выглядело как требование `JsonWriter<HttpResponseEntity<T>>`, которого никто не запрашивал, тогда как истинной причиной был реактивный контроллер в другом файле.

### Minimal reproduction

```java
@Component
@HttpController
public final class JsonGetController {

    @Json
    public record HelloWorldResponse(String greeting) {}

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/json/entity")
    public HttpResponseEntity<HelloWorldResponse> getEntity() {
        return HttpResponseEntity.of(201, new HelloWorldResponse("Hello world"));
    }
}
```

### Expected behavior

Должен использоваться уже существующий во фреймворке фабричный метод

```java
@Json
@DefaultComponent
default <T> HttpServerResponseMapper<HttpResponseEntity<T>> jsonHttpResponseEntityHttpServerResponseMapper(JsonWriter<T> writer)
```

то есть требоваться `JsonWriter<HelloWorldResponse>` — он генерируется по `@Json` на record.

### Actual behavior

```
No component found for dependency: JsonWriter<HttpResponseEntity<JsonGetController.HelloWorldResponse>>
```

### Investigation notes

В `io.koraframework.http.server.common.response.mapper.HttpServerResponseMapperModule` объявлены **два** подходящих `@Json @DefaultComponent` фабричных метода:

```java
default <T> HttpServerResponseMapper<HttpResponseEntity<T>> jsonHttpResponseEntityHttpServerResponseMapper(JsonWriter<T> writer)  // специализированный
default <T> HttpServerResponseMapper<T>                     jsonHttpServerResponseMapper(JsonWriter<T> writer)                    // общий
```

Оба подходят под запрошенный тип `HttpServerResponseMapper<HttpResponseEntity<HelloWorldResponse>>`: специализированный при `T = HelloWorldResponse`, общий при `T = HttpResponseEntity<HelloWorldResponse>`.

Первоначально это было записано как ошибка выбора фабрики: процессор печатал `Required at: HttpServerResponseMapperModule#<T>jsonHttpServerResponseMapper(JsonWriter<T>)`, из чего следовало, что выбран общий фабричный метод вместо специализированного.

**Этот вывод опровергнут.** В том же модуле был второй дефект — `ReactorController`, возвращавший `Mono<HttpServerResponse>`, для которого в 2.0 нет маппера. После удаления этого контроллера ошибка про `JsonWriter<HttpResponseEntity<…>>` исчезла сама, хотя `@Json`-метод с `HttpResponseEntity` остался нетронутым и модуль собирается. Значит специализированный маппер выбирается корректно, а сообщение было побочным эффектом другой, несвязанной неразрешённости.

Механизм (по коду `GraphBuilder`, строки 244–275): когда под запрос подходят несколько шаблонных кандидатов, строитель форкает граф на каждый и оставляет тот, что собрался. Если не собрался ни один — бросается исключение одного из форков, остальные уходят в `addSuppressed`. Пользователь видит первое из них и получает указание на компонент, который с проблемой не связан. Это объясняет наблюдаемое, но как гипотеза — точная причина требует изолированного теста в `kora-app-annotation-processor`.

Урок для миграции: **сначала убирать все заведомо неразрешимые компоненты** (опирающиеся на удалённую функциональность), и только потом анализировать оставшиеся ошибки графа.

### Workaround

Не требуется: после устранения настоящей причины сообщение исчезает. При отладке не доверяйте указанному в ошибке компоненту, если в модуле есть код, опирающийся на удалённую функциональность.

### Proposed fix

При провале всех форков выводить не одно исключение, а сводку по всем кандидатам — что именно не сошлось у каждого, чтобы была видна исходная неразрешённость, а не случайный форк.

### Resolution

Не закрыт. Приоритет понижен до Minor: поведение фреймворка корректно, страдает только диагностика.

---

## Issue: сгенерированные OpenAPI-интерфейсы недоступны вне своего пакета

- Status: Open (не расследовался)
- Severity: Major
- Type: Framework bug | Migration blocker
- Language: Java (в Kotlin-модулях проявления пока не разделены)
- Runtime: JVM
- Component: `openapi-generator` (генератор Kora)
- Affected framework module: `openapi`
- Affected example modules: `examples/java/kora-java-openapi-generator-http-client`, `guides/java/kora-java-guide-openapi-http-client-app`
- Framework commit: `66800169f`

### Description

Сгенерированные API-интерфейсы оказываются package-private, из-за чего код приложения их не видит:

```
error: PetApi is not public in io.koraframework.example.openapi.petV2.api; cannot be accessed from outside package
error: UsersApi is not public in io.koraframework.guide.openapi.httpclient.user.api; cannot be accessed from outside package
```

### Investigation notes

Не расследовано. Нужно посмотреть шаблоны генератора в `../kora/openapi` и определить, зависит ли модификатор от режима генерации (`java-client`) или от опций. До расследования это не следует считать подтверждённым дефектом: возможно, в 2.0 предполагается обращение через другой публичный тип.

### Resolution

Не закрыт.

---

## Issue (внешний инструмент, не Kora): задача OpenAPI-генератора отдаёт устаревший результат из build-кеша

- Status: Confirmed
- Severity: Major
- Type: Migration blocker
- Language: Java, Kotlin
- Runtime: JVM
- Component: Gradle-плагин `org.openapi.generator` 7.23.0 (не входит в Kora)

### Description

После смены пакета генерации (`ru.tinkoff.kora.*` → `io.koraframework.*`) задача генерации отдаёт из build-кеша вывод, сгенерированный до миграции, и компиляция падает сотнями `package ru.tinkoff.kora.* does not exist` в файлах, которых нет в исходниках.

### Steps to reproduce

1. Собрать модуль до миграции пакетов (кеш заполняется).
2. Сменить `apiPackage` / `modelPackage` / `invokerPackage`.
3. `./gradlew :examples:java:kora-java-crud:clean :examples:java:kora-java-crud:test` — падает.
4. Тот же прогон с `--no-build-cache` — успешен.

### Suspected cause

Ключ кеша задачи не учитывает пакетные опции; вдобавок задача не очищает `outputDir`, поэтому старые и новые файлы сосуществуют в одном source set.

### Workaround

Первый прогон после смены пакетов — `clean` + `--no-build-cache`. Это зафиксировано в обоих языковых гайдах и в `KORA_2_MIGRATION_STATUS.md`.

### Resolution

Обходной путь достаточен; изменение сборки примеров не требуется. Если проблема будет мешать в CI, рассмотреть явную очистку `outputDir` в `doFirst` задачи генерации.

---

## Не дефект: реактивные контроллеры удалены из HTTP-сервера

- Status: Closed (поведение по замыслу 2.0)
- Type: Removed functionality
- Affected example modules: `examples/java/kora-java-http-server` (`ReactorController`)

Метод контроллера, возвращающий `Mono<HttpServerResponse>`, не собирается:

```
No component found for dependency:
  HttpServerResponseMapper<reactor.core.publisher.Mono<HttpServerResponse>> (no tags)
```

Проверено по исходникам: в `http/http-server-common/src/main` нет ни одного упоминания `reactor`/`Mono`, то есть мапперы для реактивных возвратов фреймворком не предоставляются. Это соответствует синхронной модели 2.0, а не дефекту. Пример, демонстрирующий реактивный контроллер, подлежит переработке или удалению с отметкой в обоих языковых гайдах — решение фиксируется в `KORA_2_MIGRATION_STATUS.md`.
