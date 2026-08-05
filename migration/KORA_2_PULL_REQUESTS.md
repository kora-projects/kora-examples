# Готовые к отправке изменения во фреймворк

Все изменения лежат **локально** в `../kora` и **не отправлены**: ветки не запушены, PR не создавались.
Каждая ветка отведена от `master` и содержит один логический фикс с регрессионным тестом.

Для локальной работы обе ветки сведены в `integration/migration-fixes` — именно с неё публикуется
Maven Local, чтобы фиксы не затирали друг друга. Для отправки использовать исходные ветки, а не интеграционную.

| Ветка | Коммит | Модуль | Статус |
|---|---|---|---|
| `fix/http-client-json-response-entity-mapper-tag` | `3ef4560af` | `http/http-client-common` | готово к PR |
| `fix/kafka-listener-exception-param-type-use` | `0c8092194` | `kafka/kafka-annotation-processor` | готово к PR |

---

## PR 1 — `fix(http-client): tag JSON response entity mapper with @Json`

**Ветка:** `fix/http-client-json-response-entity-mapper-tag`
**Затронутый модуль:** `http/http-client-common`

### Проблема

Любой метод HTTP-клиента, возвращающий `HttpResponseEntity<T>`, не компилируется в приложении, где в графе есть `JsonReader<T>`:

```
error: Multiple components match dependency:
    HttpClientResponseMapper<HttpResponseEntity<String>> (no tags)
  Candidates:
    - factory HttpClientResponseMapperModule#httpClientResponseEntityResponseMapper(...)
    - factory HttpClientResponseMapperModule#httpClientResponseJsonEntityResponseMapper(...)
```

### Воспроизведение

```java
@HttpClient("httpClient.default")
public interface SomeClient {
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    HttpResponseEntity<String> get();
}
```

в приложении, где есть любой `JsonReader<String>` (то есть практически в любом сервисе с JSON).

### Корневая причина

В `HttpClientResponseMapperModule` каждая JSON-фабрика помечена `@Json` — `httpClientJsonEitherResponseMapper`, `httpClientJsonEitherResponseEntityResponseMapper`, — **кроме** `httpClientResponseJsonEntityResponseMapper(JsonReader<T>)`. Без тега две `@DefaultComponent`-шаблонные фабрики дают один и тот же нетегированный тип `HttpClientResponseMapper<HttpResponseEntity<T>>`, и граф не может выбрать между ними. JSON-вариант по замыслу доступен только под тегом `@Json`.

### Реализованный фикс

Одна строка — аннотация `@Json` на `httpClientResponseJsonEntityResponseMapper`.

### Покрытие тестами

`HttpClientExtensionTest#testExtensionResponseEntityWhenJsonReaderIsPresent`.

Проверено обеими сторонами: без фикса тест падает с `Multiple components match dependency`, с фиксом проходит. Существовавший `testExtensionWithoutTag` дефект не ловил — в его графе нет `JsonReader`.

Прогнаны: `:http:http-client-common:test`, `:http:http-client-annotation-processor:test`, `:http:http-client-symbol-processor:test` — зелёные.

### Влияние на совместимость

Код, полагавшийся на неявный JSON-маппинг `HttpResponseEntity<T>` без `@Json`, должен теперь указывать `@Json` явно. Практического слома нет: раньше такой код не собирался вовсе.

### Связанные модули kora-examples

`examples/java/kora-java-http-client` — после фикса компилируется, 9/9 тестов проходят.

---

## PR 2 — `fix(kafka): detect Exception listener parameter by type element, not by printed type`

**Ветка:** `fix/kafka-listener-exception-param-type-use`
**Затронутый модуль:** `kafka/kafka-annotation-processor`

### Проблема

Листенер с параметром-исключением отвергается процессором, если параметр помечен JSpecify-аннотацией:

```
error: Kafka record listener method has unsupported parameter:
    java.lang.@org.jspecify.annotations.Nullable Exception
```

Тот же листенер без `@Nullable` компилируется нормально.

### Воспроизведение

```java
@KafkaListener("kafka.consumer.my-listener")
public void process(@Nullable ConsumerRecord<String, String> record,
                    @Nullable Exception exception) {
}
```

### Корневая причина

`KafkaUtils.isAnyException` сравнивал `tm.toString()` со строкой `"java.lang.Exception"`. JSpecify-аннотации являются type-use, поэтому `toString()` печатает их внутри имени типа: `java.lang.@org.jspecify.annotations.Nullable Exception`. Совпадения нет, параметр классифицируется как `ConsumerParameter.Unknown` и отвергается.

Это единственная проверка в `KafkaUtils`, сделанная по строке: `isConsumer`, `isHeaders`, `isKeyDeserializationException`, `isValueDeserializationException` разрешают тип через `dt.asElement()` и потому к аннотациям устойчивы.

Актуальность повышена тем, что в Kora 2.0 JSpecify — штатный способ разметки нуллабельности, то есть под удар попадает обычный прикладной код.

### Реализованный фикс

`isAnyException` резолвит элемент типа и сравнивает квалифицированное имя — так же, как соседние проверки в этом же классе.

### Покрытие тестами

`KafkaListenerRecordTest#testProcessRecordAndParseExceptionWithTypeUseAnnotation`.

Проверено обеими сторонами: без фикса тест падает с исходной ошибкой `Kafka record listener method has unsupported parameter`, с фиксом проходит.

### Влияние на совместимость

Строго расширяющее: ранее отвергавшиеся сигнатуры теперь принимаются. Поведение листенеров без аннотаций не меняется.

### Связанные модули kora-examples

`examples/java/kora-java-kafka` — после фикса компилируется.

---

## PR 3 — `fix(openapi-generator): generate public client API interfaces`

- Ветка: `fix/openapi-client-api-interface-public`
- База: `master` @ `66800169f`
- Коммит: `a5e7694e6`
- Затронутый модуль: `openapi/openapi-generator`

### Проблема

Интерфейсы, которые генератор Kora выпускает в режиме `java-client`, получаются package-private,
поэтому приложение не может ими пользоваться:

```
error: PetApi is not public in io.koraframework.example.openapi.petV2.api; cannot be accessed from outside package
```

### Воспроизведение

Сгенерировать любой клиент в режиме `java-client` и обратиться к интерфейсу из другого пакета —
именно так устроен каждый реальный проект, где спека лежит в своём пакете.

### Корневая причина

`ClientApiGenerator` создаёт тип через `TypeSpec.interfaceBuilder(...)`, не задавая модификаторы.
JavaPoet ничего не подставляет по умолчанию, и интерфейс выходит с пакетной видимостью.
Серверный генератор проставляет `PUBLIC` явно, поэтому там проблемы нет.

### Реализованный фикс

`.addModifiers(Modifier.PUBLIC)` в `ClientApiGenerator` и для вложенных типов
в `ClientRequestMapperGenerator`.

### Покрытие тестами

`HttpClientJavaOpenapiTest#clientApiInterfaceIsPublic`.

### Влияние на совместимость

Обратно совместимо — только расширение видимости.

### Связанные модули kora-examples

`examples/java/kora-java-openapi-generator-http-client`, `guides/java/kora-java-guide-openapi-http-client-app`.

---

## PR 4 — `fix(camunda-zeebe-worker): stop marking job worker annotations as AOP annotations`

- Ветка: `fix/zeebe-worker-annotations-not-aop`
- База: `master` @ `66800169f`
- Коммит: `9873eeb69`
- Затронутые модули: `experimental/camunda-zeebe-worker`, `experimental/camunda-zeebe-worker-annotation-processor`

### Проблема

`@Component`-класс с `public`-методом под `@JobWorker` не попадает в граф. Компилятор советует
добавить `@Component`, который уже стоит:

```
error: No component found for dependency:
    io.koraframework.example.camunda.zeebe.Step1JobWorker (no tags)
  ^--- io.koraframework.example.camunda.zeebe.Step1JobWorker    [MISSING]
  Fix:
    - Add @Component to an implementation of io.koraframework.example.camunda.zeebe.Step1JobWorker.
```

### Воспроизведение

```java
@Component
public final class Handler {

    @JobWorker("worker")
    public void handle(@JobVariable String var1) {}
}
```

в приложении, где есть корень, зависящий от `All<KoraJobWorker>`. С package-private `handle` всё
собирается — именно поэтому дефект не виден в текущих тестах процессора.

### Корневая причина

`KoraAppProcessor.processComponents` пропускает `@Component`-класс, если `hasAopAnnotations` вернул
`true`, рассчитывая, что компонентом станет сгенерированный AOP-прокси. Но `AopAnnotationProcessor`
обрабатывает только те аннотации, которые заявили зарегистрированные `KoraAspectFactory`, а для
`@JobWorker` / `@JobVariable` / `@JobVariables` аспекта нет ни одного. Прокси не появляется, класс
исчезает. `hasAopAnnotations` при этом смотрит только `public`/`protected` методы, поэтому дефект
зависит от модификатора метода-обработчика.

### Реализованный фикс

Снята мета-аннотация `@AopAnnotation` с трёх zeebe-аннотаций. Все прочие кодогенерирующие аннотации
Kora (`@ScheduleAtFixedRate`, `@KafkaListener`) её тоже не несут. Аспекты вроде `@Log` или `@Timeout`
на методах воркера продолжают работать — они включают прокси сами.

### Покрытие тестами

`ZeebeWorkerTests#workerWithPublicMethodIsResolvedInGraph` — прогоняет `KoraAppProcessor`,
`AopAnnotationProcessor` и `ZeebeWorkerAnnotationProcessor` вместе. Проверено, что без фикса тест
падает с `Handler [MISSING]`, с фиксом проходит.

### Влияние на совместимость

Обратно совместимо: то, что собиралось, продолжает собираться; то, что ломалось, начинает работать.

### Связанные модули kora-examples

`examples/java/kora-java-camunda-zeebe-worker`.

### Замечание для ревью

Дефект шире zeebe: любая `@AopAnnotation` без зарегистрированного аспекта молча убирает
`@Component`-класс из графа. Этот PR закрывает конкретный случай; отдельно стоит обсудить,
не должен ли `KoraAppProcessor` сверяться с реестром аспектов или явно ругаться на
AOP-аннотацию без аспекта.

---

## PR 5 — `fix(camunda-zeebe-worker): raise a BPMN error for JobWorkerException again`

- Ветка: `fix/zeebe-worker-exception-throws-bpmn-error`
- База: `master` @ `66800169f`
- Коммит: `b26022694`
- Затронутый модуль: `experimental/camunda-zeebe-worker-annotation-processor`

### Проблема

`JobWorkerException`, брошенный из воркера, должен поднимать BPMN-ошибку с указанным кодом,
чтобы процесс ушёл по boundary error event. Сейчас исключение просто пробрасывается: задача
падает, ретраится до исчерпания бюджета, ветка обработки ошибки не выполняется.

### Воспроизведение

Процесс с boundary error event `errorCode="DOESNT_WORK"` и воркер:

```java
@JobWorker("fail")
public void handle(@JobVariable int someNumber) {
    throw new JobWorkerException("DOESNT_WORK");
}
```

Экземпляр процесса не завершается.

### Корневая причина

Коммит `63ad1886c` («Simplify zeebe client telemetry», #527) убрал из `WrappedJobHandler`
метод `createErrorCommand(...)`, который строил
`client.newThrowErrorCommand(job.getKey()).errorCode(...).errorMessage(...)`,
и не поставил ничего взамен: `git log -S newThrowErrorCommand` показывает только этот коммит
и исходный «Camunda 8». Генератор при этом до сих пор пишет `catch (JobWorkerException e) { throw e; }`.

Окружающая телеметрия эту команду по-прежнему ждёт — обе ветки сейчас мертвы:

- `DefaultZeebeWorkerObservation#observeFinalCommandStep` помечает `ThrowErrorCommandStep2` как `failedByUser`;
- `DefaultZeebeWorkerLoggerFactory#logJobEnd` отдельно обрабатывает `error == null && failedByUser`.

### Реализованный фикс

Команда строится там, где известен исход, — в сгенерированном воркере; `WrappedJobHandler`
не меняется, он уже отправляет возвращённую команду и наблюдает её. В BPMN-ошибку превращается
только `JobWorkerException`; остальное по-прежнему заворачивается в
`JobWorkerException("UNEXPECTED", e)` и бросается.

### Покрытие тестами

`ZeebeWorkerTests#workerJobWorkerExceptionIsTurnedIntoBpmnError` — поведенческий тест на
возвращаемую команду. Проверено, что без фикса он падает с вылетающим из `handle` исключением.

### Влияние на совместимость

Восстанавливает поведение 1.x.

### Связанные модули kora-examples

`examples/java/kora-java-camunda-zeebe-worker` — тест `ZeebeMockedTests#processDemoSuccess`.

### Порядок применения

PR 4 и PR 5 независимы, но затрагивают один тестовый файл `ZeebeWorkerTests.java` — при
последовательном применении будет тривиальный конфликт по импортам и хвосту файла.

---

## Обнаружено, но не исправлено

`Cassandra-репозиторий с CompletableFuture<T> генерирует некомпилируемый код` — подробности, репродукция и предлагаемый фикс в `KORA_2_FRAMEWORK_ISSUES.md`. Фикс требует изменения генерации внутри обёртки `observe(...).call(...)` и отдельного теста в `CassandraResultsTest`; в рамках текущего прохода не делался, модуль помечен `BLOCKED_BY_FRAMEWORK_BUG`.
