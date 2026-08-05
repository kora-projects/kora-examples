# Kora 2.0 — журнал дефектов, найденных при миграции

Здесь фиксируется всё подозрительное, что может оказаться проблемой Kora 2.0, включая неподтверждённые гипотезы.
Гипотезы помечены явно и уточняются по мере расследования. Запись здесь **не** означает, что баг подтверждён.

Источник истины по фреймворку — локальный чекаут `../kora` @ `master` (`io.koraframework:*:2.0.0-SNAPSHOT`, Maven Local).

---

## Issue: Cassandra-репозиторий с `CompletableFuture<T>` генерирует некомпилируемый код

- Status: Fixed
- Severity: Major
- Type: Framework bug
- Language: Java
- Runtime: JVM
- Component: `database-annotation-processor`, генератор Cassandra-репозиториев
- Affected framework module: `database/database-annotation-processor`
- Affected example modules: `examples/java/kora-java-database-cassandra`, `examples/graalvm/kora-java-graalvm-crud-cassandra`
- Framework commit: `66800169f`
- Related fix branch: `fix/cassandra-completable-future-return` (`389ad86fb`)

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

Заменить тип возврата на `CompletionStage<T>` (больше не требуется).

### Implemented fix

`.toCompletableFuture()` перенесён из внутренней лямбды наружу внешней обёртки `observe(...)`:

```java
CommonUtils.observe(mb, "_observation", "call", b -> { … });
// the observe(...) wrapper yields whatever the lambda returns, and the chain inside it is a
// CompletionStage; converting inside the lambda would make R unresolvable against the
// declared CompletableFuture return type
if (((DeclaredType) returnType).asElement().toString().equals(CompletableFuture.class.getCanonicalName())) {
    mb.addCode(".toCompletableFuture()");
}
```

Внешний `observe(...).call(...)` возвращает то, что вернула лямбда, а внутри неё цепочка
`prepareAsync(...).thenCompose(...)` даёт `CompletionStage`. Пока преобразование стояло внутри,
параметр `R` внешней обёртки не сводился с объявленным `CompletableFuture`. Теперь преобразование
происходит там, где значение действительно пересекает границу объявленного типа возврата.
Ветка `CompletionStage` не изменилась.

### Test coverage

`CassandraResultsTest#testReturnCompletableFutureObject` и `#testReturnCompletableFutureVoid`.
Оба падают без фикса ровно с `incompatible types: inference variable R has incompatible bounds`.
До этого в тестах были только `CompletionStage<Integer>` и `CompletionStage<Void>` — ни одного
случая с `CompletableFuture`, поэтому регрессия не ловилась.

### Compatibility impact

Чистое исправление: раньше такой код не компилировался вовсе.

### Validation

`:database:database-annotation-processor:test` целиком зелёный; затем сборка
`examples/java/kora-java-database-cassandra`.

### Resolution

Закрыт. Пометка `BLOCKED_BY_FRAMEWORK_BUG` с модулей снята.

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

- Status: Closed — не дефект (опровергнуто экспериментом)
- Severity: —
- Type: ложный след; настоящая причина — [неиспользуемый конвертер multipart-файла](#issue-генератор-openapi-требует-конвертер-для-multipart-файла-который-сам-не-использует)
- Language: Java, Kotlin
- Runtime: JVM
- Component: `kora-app-annotation-processor` (диагностика разрешения шаблонов)
- Affected example modules: `guides/java/kora-java-guide-openapi-http-server-advanced-app`, `guides/kotlin/kora-kotlin-guide-openapi-http-server-advanced-app`
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

Для `HttpServerResponseMapper<HttpResponseEntity<PayloadTO>>` подходят оба, и по тексту ошибки
казалось, что выбран более общий, из-за чего требуется несуществующий
`JsonWriter<HttpResponseEntity<PayloadTO>>` вместо `JsonWriter<PayloadTO>`.

**Опровергнуто контролируемым экспериментом.** В том же модуле была третья ошибка — недостающий
`HttpServerParameterReader<byte[]>` для multipart-загрузки. В `Application` временно добавлен
компонент-заглушка:

```java
default HttpServerParameterReader<byte[]> tempProbeReader() {
    return String::getBytes;
}
```

После этого модуль собрался целиком: **обе** ошибки про `JsonWriter<HttpResponseEntity<…>>` исчезли,
хотя ни генератор, ни `HttpServerResponseMapperModule` не менялись. Значит специализированный шаблон
`jsonHttpResponseEntityHttpServerResponseMapper` выбирается корректно, а сообщения были побочным
эффектом другой, несвязанной неразрешённости — ровно тот механизм форков `GraphBuilder`, что описан
в отдельной записи про диагностику.

### Resolution

Закрыт как не-дефект. Настоящая причина — неиспользуемый конвертер multipart-файла в генераторе
OpenAPI (запись ниже), исправлена в ветке `fix/openapi-server-multipart-file-unused-converter`.
Пометка `BLOCKED_BY_FRAMEWORK_BUG` с модуля снята.

---

## Issue: генератор OpenAPI требует конвертер для multipart-файла, который сам не использует

- Status: Fixed
- Severity: Major (блокирует сборку модуля целиком)
- Type: Framework bug (генератор кода)
- Language: Java, Kotlin
- Runtime: JVM
- Component: `openapi-generator`, режимы `java-server` и `kotlin-server`
- Affected framework module: `openapi/openapi-generator`
- Affected example modules: `guides/java/kora-java-guide-openapi-http-server-advanced-app`, `guides/kotlin/kora-kotlin-guide-openapi-http-server-advanced-app`
- Framework commit: `66800169f`
- Related fix branch: `fix/openapi-server-multipart-file-unused-converter`

### Description

Для операции с телом `multipart/form-data`, где есть поле `type: string, format: binary`,
генератор добавляет в конструктор request-маппера параметр
`HttpServerParameterReader<byte[]>` (в Kotlin — `HttpServerParameterReader<ByteArray>`),
но в теле `apply` его **не использует**: бинарная часть отдаётся как `FormMultipart.FormPart`
напрямую. Компонента `HttpServerParameterReader<byte[]>` во фреймворке нет и быть не должно —
конвертер читает `String`, а не бинарные данные. В результате граф не собирается.

### Minimal reproduction

```yaml
/data/upload:
  post:
    operationId: processUpload
    requestBody:
      required: true
      content:
        multipart/form-data:
          schema:
            type: object
            required: [file]
            properties:
              file:
                type: string
                format: binary
```

Тот же случай уже был в фикстурах генератора: `petstoreV3_form.yaml`, операции
`/form-multipart-form-data-with-object` и `/form-multipart-form-data-with-array`.
Тесты этого не ловили, потому что они только компилируют сгенерированный код — а он
компилируется, ломается лишь построение графа `@KoraApp`.

### Actual behavior

```java
class ProcessUploadFormParamRequestMapper implements HttpServerRequestMapper<...> {
    private final HttpServerParameterReader<byte[]> fileConverter;   // <- поле мёртвое

    public ProcessUploadFormParamRequestMapper(HttpServerParameterReader<byte[]> fileConverter) {
        this.fileConverter = fileConverter;
    }

    @Override
    public DataApiController.ProcessUploadFormParam apply(HttpServerRequest rq) throws IOException {
        var file = (FormMultipart.FormPart) null;
        ...
        case "file" -> { file = _part; }   // <- конвертер не вызывается
    }
}
```

```
No component found for dependency:
  io.koraframework.http.server.common.request.HttpServerParameterReader<byte[]> (no tags)
```

### Investigation notes

В `ServerRequestMapperGenerator` (Java) и `ServerRequestMappersGenerator.kt` (Kotlin) цикл,
объявляющий поля-конвертеры, пропускал только `String` и `List<String>`. Тело `mapMultipart`
при этом отдельно проверяет `formParam.isFile` и в этом случае присваивает `_part` как есть.
То есть условия пропуска в конструкторе и в теле разошлись.

Тонкость: пропускать `isFile` безусловно нельзя. Если операция объявляет и `multipart/form-data`,
и `application/x-www-form-urlencoded`, тело идёт по ветке `mapUrlEncoded`, а она конвертер вызывает.
Поэтому пропуск привязан к тому же решению, что и выбор ветки: `multipartForm && !urlEncodedForm`.

### Implemented fix

В обоих генераторах в цикл объявления конвертеров добавлено:

```java
// url-encoded wins when an operation declares both, same as the apply() body below
var multipartBody = multipartForm && !urlEncodedForm;
...
if (multipartBody && formParam.isFile) {
    // mapMultipart passes a binary part through as FormPart, so it never calls a converter
    continue;
}
```

### Test coverage

`HttpServerJavaOpenapiTest#multipartFileFormParamDoesNotAskForAConverterItNeverUses` и
одноимённый тест в `HttpServerKotlinOpenapiTest`. Проверяют на существующей фикстуре
`petstoreV3_form.yaml`, что оба multipart-маппера не объявляют `HttpServerParameterReader`,
но по-прежнему присваивают `_part`, а url-encoded маппер конвертеры сохранил.
Без фикса оба теста падают на `assertFalse(... contains("HttpServerParameterReader"))`.

### Compatibility impact

Ломающее изменение сигнатуры сгенерированного конструктора — но только для случая, который
до сих пор вообще не собирался. Работающий код затронуть не может.

### Validation

`:openapi:openapi-generator:test`; затем сборка и тесты обоих advanced-гайдов.

---

## Issue: java-генератор OpenAPI берёт минимум схемы как верхнюю границу `@Range`

- Status: Fixed
- Severity: Major (сгенерированная валидация отвергает корректные запросы)
- Type: Framework bug (генератор кода)
- Language: Java
- Runtime: JVM
- Component: `openapi-generator`, `AbstractJavaGenerator`
- Affected framework module: `openapi/openapi-generator`
- Affected example modules: `examples/java/kora-java-crud`, `examples/java/kora-java-crud-submodule`, `examples/java/kora-java-openapi-generator-http-server`, оба openapi-гайда
- Framework commit: `66800169f`
- Related fix branch: `fix/openapi-java-range-upper-bound` (`a1bfe41cb`)

### Description

`AbstractJavaGenerator.getValidation` при построении верхней границы `@Range` читал `variable.getMinimum()`.
У каждого числового ограничения, выпущенного java-генератором, `to` совпадало с `from`.

### Actual behavior

```
minimum: 1,   maximum: 100  ->  @Range(from = 1.0,   to = 1.0)
minimum: 200, maximum: 599  ->  @Range(from = 200.0, to = 200.0)
```

Случай «только minimum» ломался так же: вместо ветки с максимумом типа брался минимум.

Kotlin-генератор на тех же спецификациях выдаёт `to = 100.0` и `to = 599.0`. Так дефект и обнаружился:
java-клиент отправил `size=100` java-серверу, собранному по спецификации `1..100`, и получил
`Should be in range from '1' to '1', but was greater: 100`, тогда как Kotlin-двойник прошёл.

### Investigation notes

Тесты этого не ловили: в фикстуре `petstoreV3_validation.yaml` обе формы уже были, но выпущенную
границу никто не проверял. Тесты примеров проходили потому, что ни один не отправлял значение
выше минимума — то есть дефект жил в сгенерированном коде всех java-модулей незаметно.

### Implemented fix

Ветка максимума читает `getMaximum()`.

### Test coverage

`HttpServerJavaOpenapiTest#numericRangeUsesTheSchemaMaximumAsUpperBound` — обе формы схемы.
Без фикса падает, показывая `@Range(from = 1.0, to = 1.0)`.

---

## Issue: ни один спан не экспортируется — производный контекст OpenTelemetry теряет обёртку Kora

- Status: Fixed
- Severity: Blocker (трассировка не работает вообще)
- Type: Framework bug
- Language: Java, Kotlin (общий runtime)
- Runtime: JVM
- Component: `core/common`, `OpentelemetryContext`
- Affected framework module: `core/common`
- Affected example modules: `examples/java/kora-java-telemetry`, `examples/kotlin/kora-kotlin-telemetry`
- Framework commit: `66800169f`
- Related fix branch: `fix/otel-context-with-loses-kora-wrapper` (`3f3403fbd`)

### Description

Приложение с OTLP-экспортёром не отправляет ни одного спана. Коллектор получает метрики и больше ничего.

### Steps to reproduce

Поднять `otel/opentelemetry-collector`, запустить приложение с `tracing.exporter.endpoint`,
сделать любой HTTP-запрос. В логе коллектора появляется `MetricsExporter`, но не `TracesExporter`.

### Actual behavior

В логе приложения на каждую попытку экспорта:

```
BatchSpanProcessor$Worker exportCurrentBatch
WARNING: Exporter threw an Exception
java.lang.IllegalStateException
  at OpentelemetryContextStorage.attach(OpentelemetryContextStorage.java:12)
  at io.opentelemetry.context.Context.makeCurrent(Context.java:232)
  at io.opentelemetry.context.Context.lambda$wrap$1(Context.java:241)
  at InstrumentationUtil.suppressInstrumentation(InstrumentationUtil.java:34)
```

### Investigation notes

Kora хранит контекст OpenTelemetry в `ScopedValue`, поэтому императивная пара
`attach()`/`makeCurrent()` не реализуема и намеренно бросает исключение — всё держится на
`wrap(...)`, реализованном только в `OpentelemetryContext` через `ScopedValue.where(...)`.

`with(key, value)` возвращал `delegate.with(key, value)`, то есть обычный `ArrayBasedContext`:
обёртка терялась при первом же порождении контекста, и производный уходил в дефолтный
`Context.wrap(...)`, который вызывает `makeCurrent()`. OTLP-экспортёр порождает ровно такой
контекст на рабочем потоке `BatchSpanProcessor`, чтобы подавить инструментирование.

### Implemented fix

`with(...)` заворачивает результат обратно, как это уже делал `root()`.

### Test coverage

`OpentelemetryContextTest` — производный контекст и воспроизведённый путь подавления
инструментирования. Оба падают с `IllegalStateException` без фикса.

---

## Issue: эндпоинт метрик всегда отвечает «Metric Scraper disabled»

- Status: Fixed
- Severity: Blocker (Prometheus-скрейпинг невозможен)
- Type: Framework bug (отсутствующая привязка компонента)
- Language: Java, Kotlin (общий runtime)
- Runtime: JVM
- Component: `telemetry/micrometer-module`, `http-server-common` (`MetricsHandler`)
- Affected example modules: `guides/java/kora-java-guide-observability-app`, `guides/kotlin/kora-kotlin-guide-observability-app`
- Framework commit: `66800169f`
- Related fix branch: `fix/metrics-scraper-not-bound` (`013b66db5`)

### Description

`GET /metrics` в любом приложении Kora 2.0 отдаёт `200` с телом `# Metric Scraper disabled`,
даже когда `micrometer-module` подключён.

### Investigation notes

`MetricsHandler` берёт из графа `ValueOf<Optional<MetricsScraper>>`. Компонента такого типа
не предоставляет никто: `PrometheusMeterRegistryWrapper` интерфейс реализует, но фабрика
`MetricsModule#prometheusMeterRegistry` объявлена как `Wrapped<MeterRegistry>`, поэтому граф
знает её только как `MeterRegistry`. Поиск по всему фреймворку даёт лишь три упоминания
`MetricsScraper`: интерфейс, обёртка и обработчик — ни одной фабрики.

### Implemented fix

`MetricsModule` дополнительно отдаёт `MetricsScraper` поверх реестра (`@DefaultComponent`,
чтобы приложение могло связать свой).

### Test coverage

`MetricsScraperBindingTest` — поведение скрейпера для Prometheus- и не-Prometheus-реестра.
Тест не может упасть на старом коде обычным способом: проверяемый метод и есть то, чего
не хватало. Сквозное доказательство — контейнер, собранный до фикса, отдаёт
`# Metric Scraper disabled`, после фикса — настоящий Prometheus-вывод.

### Смежное, но не дефект

После фикса `/metrics` отдаёт JVM-метрики, но не `http_server_*`: в 2.0
`TelemetryConfig.MetricsConfig.enabled()` по умолчанию `false`. Это изменение поведения,
описанное в обоих руководствах по миграции.

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

- Status: Confirmed (механизм подтверждён контролируемым экспериментом, не исправляется — нужен дизайн вывода)
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

Механизм (по коду `GraphBuilder`, строки 244–275): когда под запрос подходят несколько шаблонных кандидатов, строитель форкает граф на каждый и оставляет тот, что собрался. Ключевая деталь — `fork.build()` достраивает **весь оставшийся граф**, а не только поддерево спорной зависимости. Поэтому любая неразрешимость где угодно ниже по графу роняет все форки сразу. Если не собрался ни один — бросается исключение одного из форков, остальные уходят в `addSuppressed`. Пользователь видит первое из них и получает указание на компонент, который с проблемой не связан.

**Подтверждено вторым, контролируемым экспериментом** на `guides/java/kora-java-guide-openapi-http-server-advanced-app`. Там из трёх ошибок компиляции две были про `JsonWriter<HttpResponseEntity<PayloadTO>>` и `JsonWriter<HttpResponseEntity<ErrorResponseTO>>`, а третья — про недостающий `HttpServerParameterReader<byte[]>` в совершенно другой операции (multipart-загрузка). Добавление одного компонента-заглушки, закрывающего только третью ошибку, убрало все три. Это ровно предсказание механизма: настоящая неразрешённость была одна, две остальные — обломки перебора форков.

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

---

## Issue: gRPC-приложение завершается сразу после старта сервера

- Status: Fixed
- Severity: Blocker
- Type: Regression
- Language: Java, Kotlin (общий runtime-модуль)
- Runtime: JVM
- Component: `grpc-server`
- Affected framework module: `grpc/grpc-server`
- Affected example modules: `examples/java/kora-java-grpc-server`, `examples/kotlin/kora-kotlin-grpc-server`, `guides/kotlin/kora-kotlin-guide-grpc-server-app`, `guides/kotlin/kora-kotlin-guide-grpc-server-advanced-app`
- Framework commit: `66800169f`
- Related fix branch: `fix/grpc-server-keeps-process-alive` (`62be6855b`)

### Description

Приложение, у которого единственный сервер — gRPC, стартует и немедленно завершает работу. Тесты во всех четырёх модулях падают с `StatusRuntimeException: UNAVAILABLE / Connection refused`, потому что контейнер к моменту запроса уже остановлен.

### Steps to reproduce

```shell
./gradlew :examples:java:kora-java-grpc-server:distTar
docker build -t kora-grpc-manual examples/java/kora-java-grpc-server
docker run -d --name t -e GRPC_PORT=8090 -p 18090:8090 kora-grpc-manual
docker inspect -f '{{.State.Status}} exit={{.State.ExitCode}}' t     # exited exit=0
```

### Relevant output

В логе контейнера — только исключение фонового потока перезагрузки конфигурации; строк `Application initialized` и `gRPC Server started` нет (асинхронный аппендер не успевает сброситься):

```
Exception in thread "config-reload" java.lang.IllegalStateException: Graph node value was not initialized:
  [#0] interface io.koraframework.config.common.origin.ConfigOrigin (@Tag(ApplicationConfig)) (0 dependencies)
	at io.koraframework.config.common.ConfigWatcher.watchJob(ConfigWatcher.java:75)
```

### Investigation notes

- **Доказано:** контейнер завершается с кодом `0`. При неудачном старте `KoraApplication.run` пишет ошибку и делает `System.exit(-1)`, то есть код был бы `255`. Ноль означает штатное завершение JVM — не осталось ни одного non-daemon потока.
- `KoraApplication.run` не блокирует: инициализирует граф, вешает shutdown hook и возвращает управление. Процесс живёт ровно столько, сколько живут потоки компонентов.
- `GrpcServer.init()` вызывает `server.start()` и выходит; `awaitTermination` вызывается только в `release()`.
- В 2.0 транспорт сменился на `OkHttpServerBuilder` с `directExecutor()` и `VirtualThreadExecutorTransportFilter` (`GrpcServerFactoryModule.grpcServerBuilder`). Виртуальные потоки — daemon, поэтому удерживать JVM нечему.
- HTTP-примеры выживают по другой причине: XNIO-воркер Undertow работает на платформенных non-daemon потоках. То есть время жизни процесса в 2.0 держится на транспорте, а не на явном контракте.
- Отдельно замечено: `ConfigWatcher.watchJob` читает узел графа до его инициализации. На жизненный цикл не влияет, но исключение в фоновом потоке шумит в логе.

### Suspected cause

Смена транспорта gRPC на виртуальные потоки без явного удержания процесса.

### Implemented fix

Первоначальный вывод «нужен выбор дизайна на стороне фреймворка» **оказался неверным**.
`http-server-undertow` уже решает ровно эту задачу и явно это документирует —
`XnioLifecycle.init()` заводит выделенный non-daemon поток с комментарием:

```java
// XnioWorker will be daemon despite flag .setDaemon(false) if the thread it is started from is daemon (virtual thread)
```

То есть «серверный компонент удерживает процесс собственным non-daemon потоком» — это уже
установленный в фреймворке контракт, а не открытый вопрос. gRPC-сервер просто перестал его
выполнять при смене транспорта на виртуальные потоки.

`GrpcServer.init()` теперь заводит такой поток, ожидающий `server.awaitTermination()`, а
`release()` прерывает его после завершения shutdown-последовательности — чтобы поток не пережил
`shutdownNow()` и не подвесил JVM.

### Test coverage

`GrpcServerProcessLifetimeTest#runningServerHoldsANonDaemonThread` поднимает настоящий сервер
на порту 0 и проверяет, что живой non-daemon поток есть во время работы и исчезает после
`release()`. Без фикса падает на первой проверке.

### Остаётся открытым

Более широкий вопрос — должно ли время жизни приложения быть явным контрактом
`KoraApplication.run`, а не следствием того, какой транспорт использует конкретный компонент.
Этот фикс такому решению не мешает.

### Workaround

Не требуется.
