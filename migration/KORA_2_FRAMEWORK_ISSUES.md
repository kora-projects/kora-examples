# Kora 2.0 — журнал дефектов, найденных при миграции

Здесь фиксируется всё подозрительное, что может оказаться проблемой Kora 2.0, включая неподтверждённые гипотезы.
Гипотезы помечены явно и уточняются по мере расследования. Запись здесь **не** означает, что баг подтверждён.

Источник истины по фреймворку — локальный чекаут `../kora` @ `master` (`io.koraframework:*:2.0.0-SNAPSHOT`, Maven Local).

---

## Issue: Cassandra-репозиторий с `CompletableFuture<T>` генерирует некомпилируемый код

- Status: Confirmed (не исправлен)
- Severity: Major
- Type: Framework bug
- Language: Java
- Runtime: JVM
- Component: `database-annotation-processor`, генератор Cassandra-репозиториев
- Affected framework module: `database/database-annotation-processor`
- Affected example modules: `examples/java/kora-java-database-cassandra`
- Framework commit: `66800169f`

### Description

Метод Cassandra-репозитория, возвращающий `CompletableFuture<T>` (а не `CompletionStage<T>`), приводит к генерации кода, который не компилируется.

### Minimal reproduction

```java
@Repository
public interface CassandraCrudAsyncRepository extends CassandraRepository {

    @EntityCassandra
    record Entity(String id, @Column("value1") int field1, String value2, @Nullable String value3) {}

    @Query("SELECT * FROM entities WHERE id = :id")
    CompletableFuture<Entity> findById(String id);   // CompletionStage<Entity> работает
}
```

### Actual behavior

```
$CassandraCrudAsyncRepository_Impl.java:110: error: incompatible types: inference variable R has incompatible bounds
  .call(() -> {
    upper bounds: CompletableFuture<Entity>,Object
```

### Investigation notes

Поддержка `CompletableFuture` в генераторе **заявлена явно** — `CassandraRepositoryGenerator.java:143-145`:

```java
if (((DeclaredType) returnType).asElement().toString().equals(CompletableFuture.class.getCanonicalName())) {
    st.add(".toCompletableFuture()");
}
```

Цепочка строится внутри обёртки `CommonUtils.observe(..., "call", ...)`, и добавленный `.toCompletableFuture()` ломает вывод типа параметра `R` у этой обёртки.

Тесты фреймворка покрывают `CompletionStage<Integer>` и `CompletionStage<Void>` (`CassandraResultsTest`), но **ни одного теста с `CompletableFuture`** в `database-annotation-processor` нет — поэтому регрессия не ловится.

### Workaround

Заменить тип возврата на `CompletionStage<T>`.

### Proposed fix

Привести тип выражения внутри `observe(...).call(...)` к `CompletionStage`, а `.toCompletableFuture()` применять снаружи обёртки; добавить тест с `CompletableFuture<T>` в `CassandraResultsTest`.

### Resolution

Не закрыт. Модуль `kora-java-database-cassandra` остаётся `BLOCKED_BY_FRAMEWORK_BUG`: код примера корректен и демонстрирует заявленную функциональность — подгонять его под баг не стали.

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

## Issue: `@Component` job worker с `public`-методом молча исчезает из графа

- Status: **Fixed** (локально, готово к PR)
- Severity: Blocker
- Type: Framework bug
- Language: Java (Kotlin/KSP не проверялся — модуль не мигрирован)
- Runtime: JVM
- Component: `@KoraApp` (разрешение компонентов), AOP-процессор
- Affected framework module: `experimental/camunda-zeebe-worker`
- Affected example modules: `examples/java/kora-java-camunda-zeebe-worker`
- Framework commit: база `66800169f`, фикс `9873eeb69`
- Related fix branch: `fix/zeebe-worker-annotations-not-aop` (локальная, **не отправлена**)
- Related PR: не создавался

### Description

Класс, помеченный `@Component`, с `public`-методом под `@JobWorker`, не попадает в граф зависимостей.
Сгенерированный `$X_handle_KoraJobWorker` требует `X`, а компилятор сообщает, что такого компонента нет.

### Actual behavior

```
error: No component found for dependency:
  public $Step1JobWorker_handle_KoraJobWorker(Step1JobWorker handler, ZeebeWorkerConfig config, ...)
    io.koraframework.example.camunda.zeebe.Step1JobWorker (no tags)
  ...
  ^--- io.koraframework.example.camunda.zeebe.Step1JobWorker    [MISSING]

  Fix:
    - Add @Component to an implementation of io.koraframework.example.camunda.zeebe.Step1JobWorker.
```

Подсказка вводит в заблуждение: `@Component` на классе **уже стоит**.

### Investigation notes

`KoraAppProcessor.processComponents` регистрирует `@Component`-класс только если `CommonUtils.hasAopAnnotations(typeElement)` вернул `false`:

```java
if (!CommonUtils.hasAopAnnotations(typeElement)) {
    this.components.add(typeElement);
}
```

Расчёт такой, что для класса с AOP-аннотациями компонентом станет сгенерированный прокси `$X__AopProxy`.
Но `AopAnnotationProcessor.getSupportedAnnotationTypes()` собирается из аннотаций, которые заявили
зарегистрированные `KoraAspectFactory`. Для `@JobWorker` / `@JobVariable` / `@JobVariables` аспекта нет
ни одного (`grep -rln KoraAspectFactory experimental/camunda-zeebe-worker*` пусто), поэтому прокси не
генерируется никогда — класс просто пропадает.

`hasAopAnnotations` смотрит только на `public` и `protected` методы:

```java
var methods = CommonUtils.findMethods(typeElement, m -> m.contains(Modifier.PUBLIC) || m.contains(Modifier.PROTECTED));
```

Поэтому в существующих тестах `ZeebeWorkerTests` дефекта не видно — там все `handle` объявлены
package-private. Экспериментально: перевод `Step1JobWorker.handle` в package-private убирает ошибку
для него, и она сразу всплывает на `Step2JobWorker` — то есть затронуты все три воркера примера.

Все прочие кодогенерирующие аннотации Kora (`@ScheduleAtFixedRate`, `@KafkaListener`) `@AopAnnotation`
не помечены. Zeebe-аннотации — единственные `@AopAnnotation` без реализации аспекта.

### Implemented fix

Снята мета-аннотация `@AopAnnotation` с `@JobWorker`, `@JobVariable`, `@JobVariables`.
Аспекты вроде `@Log` или `@Timeout` на методах воркера продолжают работать: такие аннотации
включают прокси сами по себе.

### Test coverage

`ZeebeWorkerTests#workerWithPublicMethodIsResolvedInGraph` — прогоняет вместе `KoraAppProcessor`,
`AopAnnotationProcessor` и `ZeebeWorkerAnnotationProcessor` над `@Component`-воркером с `public`-методом
и проверяет, что граф собирается. Проверено, что тест **падает без фикса** (`Handler [MISSING]`).
AOP-процессор включён в тест намеренно — иначе можно было бы возразить, что в бою прокси всё же
сгенерировался бы.

### Compatibility impact

Обратно совместимо. Классы, которые собирались раньше (package-private `handle`), продолжают
собираться; ломавшиеся — начинают.

### Validation

`:experimental:camunda-zeebe-worker-annotation-processor:test` — зелёный (6 тестов);
`examples/java/kora-java-camunda-zeebe-worker` — компилируется.

### Обобщение

Дефект шире, чем zeebe: **любая** `@AopAnnotation` без зарегистрированного аспекта убирает
`@Component`-класс из графа без внятной диагностики. Более глубокий вариант фикса — научить
`KoraAppProcessor` сверяться с реестром аспектов, а не с фактом наличия мета-аннотации, и/или
выдавать ошибку "AOP-аннотация без аспекта" вместо молчаливого пропуска.

---

## Issue: `JobWorkerException` больше не поднимает BPMN-ошибку

- Status: **Fixed** (локально, готово к PR)
- Severity: Major
- Type: Framework bug (регрессия 2.0)
- Language: Java (Kotlin/KSP-генератор не проверялся)
- Runtime: JVM
- Component: `camunda-zeebe-worker-annotation-processor`
- Affected framework module: `experimental/camunda-zeebe-worker-annotation-processor`
- Affected example modules: `examples/java/kora-java-camunda-zeebe-worker`
- Framework commit: база `66800169f`, фикс `b26022694`
- Related fix branch: `fix/zeebe-worker-exception-throws-bpmn-error` (локальная, **не отправлена**)
- Related PR: не создавался

### Description

Брошенный из воркера `JobWorkerException` должен приводить к BPMN-ошибке с указанным кодом,
чтобы процесс ушёл по boundary error event. В 2.0 исключение просто пробрасывается: задача падает,
ретраится до исчерпания бюджета, ветка обработки ошибки не выполняется.

### Steps to reproduce

Процесс `bpm/demo.bpmn` в примере: у задачи `fail` есть boundary error event с `errorCode="DOESNT_WORK"`,
воркер бросает `new JobWorkerException("DOESNT_WORK")`.

### Actual behavior

```
io.koraframework.camunda.zeebe.worker.exception.JobWorkerException: [DOESNT_WORK]Failed with code: DOESNT_WORK
    at ...Step3JobWorker.handle(Step3JobWorker.java:22)
    at ...$Step3JobWorker_handle_KoraJobWorker.handle($Step3JobWorker_handle_KoraJobWorker.java:50)
    at io.koraframework.camunda.zeebe.worker.WrappedJobHandler.handle(WrappedJobHandler.java:49)
```

(три раза — по числу ретраев), затем тест `ZeebeMockedTests#processDemoSuccess` отваливается по
`ConditionTimeoutException`, так как экземпляр процесса не завершается.

### Investigation notes

Генератор выпускает

```java
} catch (JobWorkerException e) {
    throw e;
}
```

Команду `newThrowErrorCommand` не строит никто: `git log -S newThrowErrorCommand` даёт коммит
`63ad1886c` «Simplify zeebe client telemetry (#527)», который выкинул из `WrappedJobHandler`
метод `createErrorCommand(...)` вместе с обработкой `JobWorkerException`, ничего не поставив взамен.

При этом окружающая телеметрия по-прежнему рассчитывает на эту команду:

- `DefaultZeebeWorkerObservation#observeFinalCommandStep` выставляет `failedByUser` именно
  для `ThrowErrorCommandStep2` — сейчас эта ветка мертва;
- `DefaultZeebeWorkerLoggerFactory#logJobEnd` умеет случай `error == null && failedByUser`
  и пишет `exceptionType = "ErrorStep"` — тоже мёртвая ветка.

### Implemented fix

Команда строится там, где известен исход, — в сгенерированном воркере:

```java
} catch (JobWorkerException e) {
    var _error = client.newThrowErrorCommand(job).errorCode(e.getCode()).errorMessage(e.getMessage());
    if (e.getVariables() != null) {
        _error.variables(e.getVariables());
    }
    return _error;
}
```

`WrappedJobHandler` не трогается: он уже отправляет то, что вернул воркер, и наблюдает команду.
В BPMN-ошибку превращается **только** `JobWorkerException`; всё остальное по-прежнему
заворачивается в `JobWorkerException("UNEXPECTED", e)` и бросается, то есть непредвиденные сбои
продолжают ронять задачу, а не уходят молча по ветке ошибки.

### Test coverage

`ZeebeWorkerTests#workerJobWorkerExceptionIsTurnedIntoBpmnError` — поведенческий тест: собирает
воркер, вызывает `handle` с мок-клиентом и проверяет, что вернулась команда throw error.
Проверено, что без фикса тест падает с вылетающим из `handle` исключением.

### Compatibility impact

Восстанавливает поведение 1.x. Код, который (случайно) полагался на ретраи вместо BPMN-ошибки,
изменит поведение — но это и есть исправляемый дефект.

### Validation

`:experimental:camunda-zeebe-worker-annotation-processor:test` — зелёный.

---

## Issue: генератор OpenAPI схлопывает две одинаковые именованные схемы в одну

- Status: Observed (не расследовался)
- Severity: Minor — ломает компиляцию один раз, чинится сменой типа в сигнатуре
- Type: Изменение поведения генератора
- Language: Java (Kotlin-режимы не проверялись)
- Runtime: JVM
- Component: `openapi-generator`, режим `java-server`
- Affected framework module: `openapi/openapi-generator`
- Affected example modules: `examples/java/kora-java-crud-submodule`
- Framework commit: `66800169f`

### Description

В спеке объявлены две разные именованные схемы с идентичным содержимым:

```yaml
VetCreateTO:
  allOf: [ { $ref: '#/components/schemas/VetFieldTO' } ]
VetUpdateTO:
  allOf: [ { $ref: '#/components/schemas/VetFieldTO' } ]
```

Сгенерирована только `VetCreateTO`; `VetUpdateTO` отсутствует, и `VetApiDelegate#updateVet`
принимает `VetCreateTO`. Код примера, написанный под 1.x, ссылался на `VetUpdateTO` — значит
в 1.x модель генерировалась.

### Actual behavior

```
error: cannot find symbol
  import io.koraframework.example.submodule.openapi.http.server.model.VetUpdateTO;
```

### Investigation notes

Не расследовано, дедупликация одинаковых схем может быть намеренной (в upstream
openapi-generator есть соответствующие опции). Проверять надо две вещи: делает ли это сам
upstream-генератор или форк Kora, и управляется ли поведение опцией.

### Workaround

Привести сигнатуру к сгенерированной модели (`VetCreateTO`) либо сделать схемы различимыми,
добавив в одну из них собственное поле или описание.

### Resolution

Не закрыт. В примере применён обходной путь.

---

## Issue: для `HttpResponseEntity<T>` в sealed-ответе выбирается не тот шаблонный маппер

- Status: Investigating
- Severity: Major (блокирует модуль)
- Type: Framework bug (разрешение шаблонных компонентов) либо генератор OpenAPI
- Language: Java
- Runtime: JVM
- Component: `kora-app-annotation-processor` (разрешение шаблонов) / `openapi-generator`, режим `java-server`
- Affected example modules: `guides/java/kora-java-guide-openapi-http-server-advanced-app`
- Framework commit: `66800169f`

### Actual behavior

```
error: No component found for dependency:
    JsonWriter<HttpResponseEntity<PayloadTO>> (no tags)

  Required at:
    HttpServerResponseMapperModule#<T>jsonHttpServerResponseMapper(JsonWriter<T>)

  Dependency resolution path:
    ^--- component  DataApiServerResponseMappers.MappingByCodeApiResponseMapper
    ^--- factory  HttpServerResponseMapperModule#jsonHttpServerResponseMapper(...)
    ^--- JsonWriter<HttpResponseEntity<PayloadTO>>    [MISSING]
```

То же самое для `ErrorResponseTO`.

### Investigation notes

В `HttpServerResponseMapperModule` есть два `@Json @DefaultComponent` шаблона:

```java
default <T> HttpServerResponseMapper<HttpResponseEntity<T>> jsonHttpResponseEntityHttpServerResponseMapper(JsonWriter<T> writer)
default <T> HttpServerResponseMapper<T> jsonHttpServerResponseMapper(JsonWriter<T> writer)
```

Для `HttpServerResponseMapper<HttpResponseEntity<PayloadTO>>` подходят оба, но выбран более общий,
из-за чего требуется несуществующий `JsonWriter<HttpResponseEntity<PayloadTO>>` вместо
`JsonWriter<PayloadTO>`. Клиентский аналог этой пары в своё время оказался дефектом отсутствующего
тега (`fix/http-client-json-response-entity-mapper-tag`), но здесь **оба** метода помечены `@Json` —
значит причина другая: либо приоритет более специфичного шаблона, либо генератор запрашивает не тот тип.

Не доведено до конца: чтобы отличить одно от другого, нужно посмотреть сгенерированный
`DataApiServerResponseMappers.MappingByCodeApiResponseMapper` и понять, какой тип он объявляет
в конструкторе. Соседний рабочий модуль `examples/java/kora-java-openapi-generator-http-server`
собирается на том же генераторе, поэтому различие стоит искать в спецификации: здесь операция
`mappingByCode` описывает несколько кодов ответа, из-за чего появляется `HttpResponseEntity`.

### Resolution

Не закрыт. Модуль помечен `BLOCKED_BY_FRAMEWORK_BUG`.

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
- **Гипотеза опровергнута для `NoSuchElementException`.** Оба подвида воспроизводятся на корректном коде 2.0 и оказались самостоятельными дефектами фреймворка, разобранными отдельными записями ниже: `index T` — построение текста `UnresolvedDependencyException` (исправлено), `index E`/`index K` — потеря аргументов типа в `KspCommonUtils.fixPlatformType` (исправлено). После этих двух фиксов число падающих Kotlin-задач упало с 37 до 31 без единой правки в примерах.

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

## Issue: сгенерированные OpenAPI-интерфейсы клиента недоступны вне своего пакета

- Status: **Fixed** (локально, готово к PR)
- Severity: Major
- Type: Framework bug | Migration blocker
- Language: Java (в Kotlin-режимах генератора проявления не проверялись)
- Runtime: JVM
- Component: `openapi-generator` (генератор Kora), режим `java-client`
- Affected framework module: `openapi/openapi-generator`
- Affected example modules: `examples/java/kora-java-openapi-generator-http-client`, `guides/java/kora-java-guide-openapi-http-client-app`
- Framework commit: база `66800169f`, фикс `a5e7694e6`
- Related fix branch: `fix/openapi-client-api-interface-public` (локальная, **не отправлена**)
- Related PR: не создавался

### Description

Сгенерированные API-интерфейсы клиента оказываются package-private, из-за чего код приложения их не видит:

```
error: PetApi is not public in io.koraframework.example.openapi.petV2.api; cannot be accessed from outside package
error: UsersApi is not public in io.koraframework.guide.openapi.httpclient.user.api; cannot be accessed from outside package
```

### Investigation notes

`ClientApiGenerator` строит интерфейс через `TypeSpec.interfaceBuilder(...)` без единого модификатора.
JavaPoet не добавляет `public` сам, поэтому интерфейс получается с доступом по умолчанию.
Серверный генератор той же проблемы не имеет — там модификатор проставлен явно.

### Implemented fix

Добавлен `.addModifiers(Modifier.PUBLIC)` в `ClientApiGenerator`; то же самое для вложенных типов
маппера запросов в `ClientRequestMapperGenerator`.

### Test coverage

`HttpClientJavaOpenapiTest#clientApiInterfaceIsPublic` — проверяет, что в сгенерированном исходнике
есть `public interface `.

### Compatibility impact

Обратно совместимо: расширение видимости, ничего не ломает.

### Validation

`examples/java/kora-java-openapi-generator-http-client` компилируется; тесты PetV2 проходят
(по PetV3 остаётся открытый вопрос про порядок схем авторизации, см. `KORA_2_MIGRATION_STATUS.md`).

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

---

## Issue: построение текста ошибки DI роняет разрешение шаблонных компонентов

- Status: Fixed
- Severity: Blocker
- Type: Framework bug
- Language: Kotlin
- Runtime: Both
- Component: `kora-app-symbol-processor`, `UnresolvedDependencyException`
- Affected framework module: `core/kora-app-symbol-processor`
- Affected example modules: `examples/kotlin/kora-kotlin-helloworld`, `guides/kotlin/kora-kotlin-guide-cache-app` и ещё ~8 модулей, падавших с `No TypeParameter found for index T`
- Framework commit: `66800169f`
- Related migration guide: `KORA_2_KOTLIN_MIGRATION_GUIDE.md`
- Related fix branch: `fix/ksp-unresolved-dependency-message-generic-factory` (локально, коммит `12b5ef5a4`)
- Related PR: подготовлен, не отправлен — PR 6 в `KORA_2_PULL_REQUESTS.md`

### Description

`UnresolvedDependencyException` собирает весь текст диагностики прямо в конструкторе. Типы параметров фабричного метода рендерились `toTypeName()` с пустым `TypeParameterResolver`. Для шаблонной фабрики вида `fun <T> wrapper(holder: Holder<T>): Wrapper<T>` параметр ссылается на собственный параметр типа метода, и KotlinPoet бросал `NoSuchElementException: No TypeParameter found for index T`.

Это не только потерянная диагностика. `GraphBuilder` бросает это исключение как **штатный поток управления**: когда claim могут удовлетворить несколько шаблонов, он форкается по каждому и ловит `UnresolvedDependencyException` для тех, что не разрешились (`GraphBuilder.kt:246-259`). Построение исключения для отброшенного форка роняло KSP целиком — приложения с корректно разрешимым графом не компилировались, а сообщение не называло ни зависимости, ни места.

### Steps to reproduce

```shell
./gradlew :examples:kotlin:kora-kotlin-helloworld:kspKotlin --no-build-cache --stacktrace
```

### Minimal reproduction

```kotlin
@KoraApp
interface ExampleApplication {
    class Holder<T>(val value: T)
    class Wrapper<T>(val value: T)

    fun <T> wrapper(holder: Holder<T>): Wrapper<T> = Wrapper(holder.value)

    @Root
    fun root(wrapper: Wrapper<String>): Any = ""
}
```

### Relevant output

```
e: [ksp] java.util.NoSuchElementException: No TypeParameter found for index T
	at com.squareup.kotlinpoet.ksp.TypeParameterResolver$Companion$EMPTY$1.get(TypeParameterResolver.kt:48)
	...
	at io.koraframework.kora.app.ksp.exception.UnresolvedDependencyException$Companion.getRequestedMessage(UnresolvedDependencyException.kt:112)
	at io.koraframework.kora.app.ksp.GraphBuilder.build(GraphBuilder.kt:320)
```

### Suspected cause

Подтверждено: `toTypeName()` без резолвера параметров типа в двух местах формирования сообщения.

### Proposed fix

Строить `TypeParameterResolver` из параметров типа модуля и фабричного метода и передавать его в оба места. Неиспользуемая приватная копия `getRequestedMessage` содержала тот же дефект и удалена.

Отдельным улучшением (в фикс не входит, отмечено в описании PR): сообщение стоит вычислять лениво — сейчас полная диагностика строится для каждого отброшенного форка.

### Resolution

Исправлено. Регрессионный тест `DependencyTest#testUnresolvedDependencyOfTemplateFactoryIsReportedAsDiagnostic` без фикса падает исходным `NoSuchElementException`.

---

## Issue: `fixPlatformType` теряет аргументы типа у Java-коллекций

- Status: Fixed
- Severity: Blocker
- Type: Framework bug
- Language: Kotlin
- Runtime: Both
- Component: `symbol-processor-common`, `KspCommonUtils.fixPlatformType`
- Affected framework module: `core/symbol-processor-common`
- Affected example modules: `examples/kotlin/kora-kotlin-camunda-zeebe-worker` и все Kotlin-модули, тянущие Java-определённые модули с коллекциями в конфигурации
- Framework commit: `66800169f`
- Related migration guide: `KORA_2_KOTLIN_MIGRATION_GUIDE.md`
- Related fix branch: `fix/ksp-platform-type-drops-generic-arguments` (локально, коммит `d140f0a29`)
- Related PR: подготовлен, не отправлен — PR 7 в `KORA_2_PULL_REQUESTS.md`

### Description

`KspCommonUtils.fixPlatformType` приводит гибкий (flexible) тип, пришедший из Java, к неизменяемому Kotlin-аналогу. В ветке, где ни один аргумент типа не потребовал правки, вызывался `asType(listOf())`. В KSP это **не** «без аргументов»: подставляются собственные параметры типа декларации, и `List<String>` превращался в `List<E>`.

Ветка срабатывает ровно для `@NullMarked` Java-модулей и сгенерированных компонентов: сама коллекция остаётся гибкой по изменяемости, а её аргумент уже не платформенный — то есть `changed == false`. Полученный claim (`ConfigValueMapper<List<E>>`) KotlinPoet отрендерить не может, и KSP падал с `NoSuchElementException: No TypeParameter found for index E`, не называя ни компонента, ни зависимости.

### Steps to reproduce

```shell
./gradlew :examples:kotlin:kora-kotlin-camunda-zeebe-worker:clean :examples:kotlin:kora-kotlin-camunda-zeebe-worker:kspKotlin --no-build-cache --stacktrace
```

### Relevant output

```
e: [ksp] java.util.NoSuchElementException: No TypeParameter found for index E
	at com.squareup.kotlinpoet.ksp.TypeParameterResolver$Companion$EMPTY$1.get(TypeParameterResolver.kt:48)
	...
	at io.koraframework.kora.app.ksp.component.ComponentDependencyHelper.parseClaim(ComponentDependencyHelper.kt:113)
	at io.koraframework.kora.app.ksp.GraphBuilder.build(GraphBuilder.kt:232)
```

### Investigation notes

Первая попытка — расширить `catch (e: IllegalArgumentException)` в `parseClaim` до `NoSuchElementException` — **отвергнута**: она превращает падение в `WARNING`, KSP откладывает раунд, сборка завершается «успешно», но `ApplicationGraph` не генерируется, и пользователь получает бессмысленное `Unresolved reference 'ApplicationGraph'`. Маскировка хуже краша, изменение откачено.

### Proposed fix

В ветке `changed == false` использовать уже собранный список аргументов: `type.immutableDeclaration(resolver).asType(args)`.

### Resolution

Исправлено. Регрессионный тест `ModuleTest#testJavaModuleKeepsGenericArgumentsOfPlatformTypes` строит граф поверх `@NullMarked` Java-модуля, скомпилированного в тестовый classpath, и без фикса падает исходным исключением. Воспроизведение потребовало именно **class-файла** и `@NullMarked`: Java-исходник в той же компиляции даёт платформенный аргумент, и дефектная ветка не срабатывает.

---

## Issue: KSP-воркер Zeebe не поднимает BPMN-ошибку для `JobWorkerException`

- Status: Fixed
- Severity: Critical
- Type: Regression
- Language: Kotlin
- Runtime: Both
- Component: `camunda-zeebe-worker-symbol-processor`
- Affected framework module: `experimental/camunda-zeebe-worker-symbol-processor`
- Affected example modules: `examples/kotlin/kora-kotlin-camunda-zeebe-worker`
- Framework commit: `66800169f`
- Related migration guide: `KORA_2_KOTLIN_MIGRATION_GUIDE.md`
- Related fix branch: `fix/zeebe-worker-ksp-exception-throws-bpmn-error` (локально, коммит `8c792983b`)
- Related PR: подготовлен, не отправлен — PR 8 в `KORA_2_PULL_REQUESTS.md`

### Description

Тот же дефект, что исправлен для Java-процессора (PR 5), присутствует и в KSP-генераторе: `catch (e: JobWorkerException) { throw e }` вместо `client.newThrowErrorCommand(job)`. Джоба падает и ретраится до исчерпания бюджета, граничное событие ошибки BPMN не срабатывает — идентичное Kotlin-приложение ведёт себя иначе, чем Java-аналог.

### Steps to reproduce

`./gradlew :examples:kotlin:kora-kotlin-camunda-zeebe-worker:test` — процесс не доходит до завершения, в логе повторяется `Zeebe JobWorker failed Job`.

### Resolution

Исправлено симметрично Java-версии. Регрессионный тест `ZeebeWorkerTests#workerJobWorkerExceptionIsTurnedIntoBpmnError`; без фикса исключение выходит из `handle()`. После фикса тест примера проходит.

---

## Issue: KSP-процессоры падают при параллельном прогоне Gradle (`JacksonIOException: Stream closed`)

- Status: Confirmed (не исправлен)
- Severity: Major
- Type: Framework bug
- Language: Kotlin
- Runtime: JVM
- Component: KSP symbol processors
- Affected example modules: непостоянный набор, до 19 задач за прогон
- Framework commit: `66800169f`
- Related fix branch / PR: нет

### Description

При `./gradlew ... --continue` с параллельным выполнением часть задач `kspKotlin`/`kspTestKotlin` падает с `tools.jackson.core.exc.JacksonIOException: Stream closed`. Набор упавших модулей меняется от прогона к прогону, а каждый из них по отдельности собирается успешно.

### Steps to reproduce

```shell
./gradlew <все kotlin testClasses> --continue --no-build-cache          # 19 падений
./gradlew <те же задачи> --continue --no-build-cache --max-workers=1    # 0 падений
```

### Investigation notes

Прямое измерение: тот же набор задач в один поток дал **0** таких ошибок и 24 реальных падения вместо 37. То есть это гонка, а не дефект модулей. Показательный случай — `guides/kotlin/kora-kotlin-guide-http-server-advanced-app`: в параллельном прогоне падает, отдельной сборкой проходит и его тесты зелёные.

### Suspected cause

Разделяемое между воркерами KSP состояние Jackson (генератор/парсер), закрываемое одним воркером, пока другой ещё пишет. Точное место не локализовано.

### Workaround

Мерить и чинить миграцию прогоном с `--max-workers=1`. Все цифры в `KORA_2_MIGRATION_STATUS.md` получены именно так.

---

## Issue: `ForwardingServerBuilder<*>` не разрешается в KSP-графе gRPC-сервера

- Status: Investigating
- Severity: Blocker
- Type: Framework bug
- Language: Kotlin
- Runtime: JVM
- Component: `kora-app-symbol-processor`, `grpc-server`
- Affected example modules: `examples/kotlin/kora-kotlin-grpc-server`, `guides/kotlin/kora-kotlin-guide-grpc-server-app`, `guides/kotlin/kora-kotlin-guide-grpc-server-advanced-app`
- Framework commit: `66800169f`
- Related fix branch: частично разблокировано `fix/ksp-template-match-star-projection`

### Description

После исправления NPE на star-проекции сборка доходит до внятной диагностики:

```
No component found for dependency:
  io.grpc.ForwardingServerBuilder<*> (no tags)

Required at:
  GrpcServerFactoryModule#grpcServer(ValueOf<ForwardingServerBuilder<*>>, ValueOf<GrpcServerConfig>)
```

При этом тот же `GrpcServerFactoryModule` объявляет поставщика `@DefaultComponent @Tag(Tag.Factory.class) ForwardingServerBuilder<?> grpcServerBuilder(...)`. Java-аналог `examples/java/kora-java-grpc-server` этот же модуль разрешает и компилируется.

### Investigation notes

- Claim в диагностике помечен `(no tags)`, тогда как и метод-потребитель, и метод-поставщик объявлены с `@Tag(Tag.Factory.class)`. Совпадает ли это с ожидаемым разрешением `Tag.Factory` внутри фабричного модуля — не проверено.
- Java-путь работает, то есть расхождение именно в KSP.
- Не проверено, влияет ли на это изменение `fillMap` из `fix/ksp-template-match-star-projection` (до него это место падало с NPE, поэтому «раньше работало» сказать нельзя).

### Next step

Сравнить сгенерированные `ApplicationGraph` Java- и Kotlin-примеров и разрешение `Tag.Factory` в `TagUtils` обоих процессоров.

---

## Issue: тест gRPC-сервера не может подключиться (Java, `UNAVAILABLE`)

- Status: Open
- Severity: Major
- Type: Migration blocker
- Language: Java
- Runtime: JVM
- Component: `grpc-server`
- Affected example modules: `examples/java/kora-java-grpc-server`
- Framework commit: `66800169f`

### Description

`examples/java/kora-java-grpc-server` компилируется, но `GrpcServerTests#createUser` падает с `StatusRuntimeException: UNAVAILABLE / Connection refused` — сервер не слушает порт. Обнаружено при прогоне тестов Java-модулей, которые ранее только компилировались.

### Next step

Проверить конфигурацию `grpcServer.port` относительно 2.0 и порядок старта компонента `GrpcServer` в графе теста.
