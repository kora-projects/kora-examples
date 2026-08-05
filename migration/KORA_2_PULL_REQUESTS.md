# Готовые к отправке изменения во фреймворк

Все изменения лежат **локально** в `../kora` и **не отправлены**: ветки не запушены, PR не создавались.
Каждая ветка отведена от `master` и содержит один логический фикс с регрессионным тестом.

Для локальной работы обе ветки сведены в `integration/migration-fixes` — именно с неё публикуется
Maven Local, чтобы фиксы не затирали друг друга. Для отправки использовать исходные ветки, а не интеграционную.

| PR | Ветка | Коммит | Модуль | Статус |
|---|---|---|---|---|
| 1 | `fix/http-client-json-response-entity-mapper-tag` | `3ef4560af` | `http/http-client-common` | готово к PR |
| 2 | `fix/kafka-listener-exception-param-type-use` | `0c8092194` | `kafka/kafka-annotation-processor` | готово к PR |
| 3 | `fix/openapi-client-api-interface-public` | `1371e7fb5` | `openapi/openapi-generator` | готово к PR |
| 4 | `fix/zeebe-worker-annotations-not-aop` | `9873eeb69` | `experimental/camunda-zeebe-worker-annotation-processor` | готово к PR |
| 5 | `fix/zeebe-worker-exception-throws-bpmn-error` | `b26022694` | `experimental/camunda-zeebe-worker-annotation-processor` | готово к PR |
| 6 | `fix/ksp-unresolved-dependency-message-generic-factory` | `12b5ef5a4` | `core/kora-app-symbol-processor` | готово к PR |
| 7 | `fix/ksp-platform-type-drops-generic-arguments` | `2002a57c9` | `core/symbol-processor-common` | готово к PR |
| 8 | `fix/zeebe-worker-ksp-exception-throws-bpmn-error` | `8c792983b` | `experimental/camunda-zeebe-worker-symbol-processor` | готово к PR |
| 9 | `fix/ksp-submodule-processor-stale-symbols` | `e698d0a23` | `core/kora-app-symbol-processor` | готово к PR |
| 10 | `fix/kafka-listener-parameter-unresolved-type` | `272ecc9c0` | `kafka/kafka-symbol-processor` | готово к PR |
| 11 | `fix/kafka-publisher-observation-unbound-mdc` | `6599d21bc` | `kafka/kafka` | готово к PR |
| 12 | `fix/ksp-template-match-star-projection` | `b773f39e4` | `core/kora-app-symbol-processor` | готово к PR |
| 13 | `fix/openapi-kotlin-security-config-data-class` | `b2238931d` | `openapi/openapi-generator` | готово к PR |
| 14 | `fix/test-junit5-graph-init-lock-leak` | `aa82d0c3a` | `test/test-junit5` | готово к PR |
| 15 | `fix/openapi-server-multipart-file-unused-converter` | `dc32c616e` | `openapi/openapi-generator` | готово к PR |

> Ветки 8 и 5 трогают один и тот же файл в двух разных процессорах; при отправке порядок значения
> не имеет, но при локальном слиянии в `integration/migration-fixes` они дают конфликт, который
> разрешается в пользу обоих (Java и KSP-варианты независимы).

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

---

## PR 6 — `fix(kora-app-symbol-processor): stop template resolution from dying on message rendering`

- **Ветка:** `fix/ksp-unresolved-dependency-message-generic-factory` (локальная, не отправлена)
- **Коммит:** `12b5ef5a4`
- **Модуль фреймворка:** `core/kora-app-symbol-processor`

### Постановка задачи

`UnresolvedDependencyException` строит всю диагностику в конструкторе и рендерит объявленные типы параметров фабричного метода через `toTypeName()` с пустым `TypeParameterResolver`. Для шаблонной фабрики параметры ссылаются на собственные параметры типа метода, и KotlinPoet бросает `NoSuchElementException: No TypeParameter found for index T`.

Это не только потерянная диагностика: `GraphBuilder` бросает это исключение как штатный поток управления при переборе шаблонов-кандидатов (`GraphBuilder.kt:246-259`). Построение исключения для отброшенного форка роняло KSP, и приложение с корректно разрешимым графом не собиралось.

### Воспроизведение

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

### Причина

`toTypeName()` без резолвера параметров типа в `getRequestedMessage` и в рендере параметра-источника claim.

### Что сделано

Оба места используют резолвер, построенный из параметров типа модуля и фабричного метода. Неиспользуемая приватная копия `getRequestedMessage` с тем же дефектом удалена.

### Покрытие тестами

`DependencyTest#testUnresolvedDependencyOfTemplateFactoryIsReportedAsDiagnostic` — без фикса падает исходным `NoSuchElementException`.

### Совместимость

Поведение меняется только на пути ошибки: вместо краша выдаётся штатная диагностика, а корректные графы собираются.

### Предложение вне рамок фикса

Сообщение стоит вычислять лениво: сейчас полная диагностика строится для каждого отброшенного форка шаблона.

### Затронутые модули `kora-examples`

`examples/kotlin/kora-kotlin-helloworld`, `guides/kotlin/kora-kotlin-guide-cache-app` и ещё ~8 модулей.

---

## PR 7 — `fix(symbol-processor-common): keep generic arguments when unwrapping a Java platform type`

- **Ветка:** `fix/ksp-platform-type-drops-generic-arguments` (локальная, не отправлена)
- **Коммит:** `d140f0a29`
- **Модуль фреймворка:** `core/symbol-processor-common`

### Постановка задачи

`KspCommonUtils.fixPlatformType` приводит гибкий тип из Java к неизменяемому Kotlin-аналогу. В ветке, где ни один аргумент не требовал правки, вызывался `asType(listOf())` — в KSP это подставляет собственные параметры типа декларации, превращая `List<String>` в `List<E>`.

Ветка срабатывает для `@NullMarked` Java-модулей и сгенерированных компонентов. Claim `ConfigValueMapper<List<E>>` нерендерим, KSP падал с `NoSuchElementException: No TypeParameter found for index E` без указания компонента и зависимости. Kotlin-приложения на `camunda-zeebe-worker` и других Java-определённых модулях не собирались вовсе.

### Причина

`asType(listOf())` вместо `asType(args)` в ветке `changed == false`.

### Что сделано

Ветка переиспользует уже собранный список аргументов.

### Покрытие тестами

`ModuleTest#testJavaModuleKeepsGenericArgumentsOfPlatformTypes` строит граф поверх `@NullMarked` Java-модуля, скомпилированного в тестовый classpath. `AbstractSymbolProcessorTest` получил перегрузку `compile0`, принимающую Java-исходники — она нужна другим случаям, но конкретно этот дефект требует именно class-файла.

### Совместимость

Исправляет типы, которые раньше были заведомо некорректны; корректные типы не затрагивает.

### Затронутые модули `kora-examples`

`examples/kotlin/kora-kotlin-camunda-zeebe-worker` и все Kotlin-модули с Java-определёнными модулями, содержащими коллекции в конфигурации.

---

## PR 8 — `fix(camunda-zeebe-worker): raise a BPMN error for JobWorkerException in the KSP worker too`

- **Ветка:** `fix/zeebe-worker-ksp-exception-throws-bpmn-error` (локальная, не отправлена)
- **Коммит:** `8c792983b`
- **Модуль фреймворка:** `experimental/camunda-zeebe-worker-symbol-processor`

### Постановка задачи

Kotlin-генератор воркера содержит тот же дефект, что исправлен для Java-процессора в PR 5: `JobWorkerException` пробрасывается вместо `client.newThrowErrorCommand(...)`. Джоба падает и ретраится, граничное событие ошибки BPMN не срабатывает — идентичное Kotlin-приложение ведёт себя иначе, чем Java.

### Что сделано

Симметрично PR 5. Только `JobWorkerException` отображается в BPMN-ошибку; остальные исключения по-прежнему оборачиваются в `JobWorkerException("UNEXPECTED", e)`.

### Покрытие тестами

`ZeebeWorkerTests#workerJobWorkerExceptionIsTurnedIntoBpmnError`; без фикса исключение выходит из `handle()`. Модуль получил тестовую зависимость `mockito-core`.

### Связь с PR 5

Логически один дефект в двух реализациях генератора. PR 5 и PR 8 трогают разные модули и не конфликтуют; порядок слияния не важен.

### Затронутые модули `kora-examples`

`examples/kotlin/kora-kotlin-camunda-zeebe-worker` — после фикса тест проходит.

---

## PR 9 — `fix(kora-app-symbol-processor): resolve submodule symbols again instead of holding them`

- **Ветка:** `fix/ksp-submodule-processor-stale-symbols` (локальная, не отправлена)
- **Коммит:** `e698d0a23`
- **Модуль фреймворка:** `core/kora-app-symbol-processor`

### Постановка задачи

`KoraSubmoduleProcessor` накапливал `KSClassDeclaration` между раундами KSP и использовал их в `finish()`. В KSP2 символ, полученный в раунде N, после закрытия раунда недействителен: его резолвер уничтожен. Сборка модуля с `@KoraSubmodule` падала или молча выдавала пустой субмодуль в зависимости от того, в каком раунде объявление попало в обработку.

### Что сделано

Между раундами хранятся только **квалифицированные имена** (`MutableSet<String>` / `MutableList<String>`), плюс ссылка на последний живой `Resolver`. В `finish()` каждое имя резолвится заново через `resolver.classDeclaration(name)`. Добавлены `collectSubmodule(...)` и `generateSubmodule(resolver, submodule)`, чтобы сбор и генерация не смешивались.

### Покрытие тестами

Регрессия воспроизводится на реальном многомодульном примере: `examples/kotlin/kora-kotlin-crud-submodule`. Изолированного unit-теста нет — процессор нужно прогнать через несколько раундов, а тестовая инфраструктура `AbstractSymbolProcessorTest` этого не моделирует. Это ограничение указано честно: фикс проверен интеграционно, не юнит-тестом.

### Влияние на совместимость

Поведение не меняется, только момент резолва. Java-процессор этой проблемы не имел.

### Затронутые модули `kora-examples`

`examples/kotlin/kora-kotlin-crud-submodule` (4 подмодуля) — после фикса собирается, 7/7 тестов проходят.

---

## PR 10 — `fix(kafka-symbol-processor): report an unresolvable listener parameter instead of dying`

- **Ветка:** `fix/kafka-listener-parameter-unresolved-type` (локальная, не отправлена)
- **Коммит:** `272ecc9c0`
- **Модуль фреймворка:** `kafka/kafka-symbol-processor`

### Постановка задачи

Kotlin-аналог PR 2. `KafkaUtils` определял тип параметра слушателя сравнением **печатного представления** типа. Когда тип не резолвится (опечатка в импорте, ещё не сгенерированный класс, ошибка в другом файле), печатное представление — `<ERROR TYPE>`, ни с чем не совпадает, и процессор падал с внутренним исключением вместо диагностики. Пользователь видел стектрейс KSP, а не «не могу разрешить тип параметра».

### Что сделано

- Добавлен `private fun KSType.isClass(className: ClassName)`, сравнивающий квалифицированные имена, а не строки.
- `parseParameters` бросает `ProcessingErrorException` при `type.isError`, указывая на конкретный параметр.

### Покрытие тестами

Регрессионный тест в `kafka-symbol-processor`: слушатель с неразрешимым типом параметра. Без фикса — внутреннее исключение, с фиксом — сообщение с указанием параметра.

### Влияние на совместимость

Только диагностика. Корректный код собирается как прежде.

---

## PR 11 — `fix(kafka): publish a record when no MDC scope is bound`

- **Ветка:** `fix/kafka-publisher-observation-unbound-mdc` (локальная, не отправлена)
- **Коммит:** `6599d21bc`
- **Модуль фреймворка:** `kafka/kafka`

### Постановка задачи

`DefaultKafkaPublisherRecordObservation` безусловно вызывал `MDC.get().fork()`. В синхронной модели Kora 2.0 `MDC.VALUE` — скоуп-переменная, и вне HTTP-запроса или другого установленного скоупа она не привязана: публикация из `main`, из планировщика или из теста падала.

### Что сделано

```java
this.mdc = MDC.VALUE.isBound() ? MDC.get().fork() : new MDC();
```

Ровно то же, что делает остальной код при отсутствии скоупа: пустой контекст вместо исключения.

### Покрытие тестами

Тест публикации без установленного скоупа MDC. Без фикса падает на `MDC.get()`.

### Влияние на совместимость

Расширение области применимости: то, что раньше падало, теперь работает.

### Затронутые модули `kora-examples`

`examples/java/kora-java-kafka`, `examples/kotlin/kora-kotlin-kafka` — 48/48 тестов на два языка.

---

## PR 12 — `fix(kora-app-symbol-processor): treat a star projection as a concrete type, not a template`

- **Ветка:** `fix/ksp-template-match-star-projection` (локальная, не отправлена)
- **Коммит:** `b773f39e4`
- **Модуль фреймворка:** `core/kora-app-symbol-processor`

### Постановка задачи

KSP видит Java-подстановку `<?>` как star projection, у которой `KSTypeArgument.type == null`. Обе реализации `hasGenericVariable()` трактовали `null` как «переменная типа есть», из-за чего конкретный тип вроде `ForwardingServerBuilder<?>` уезжал в множество шаблонов. Java-процессор в аналогичной ситуации возвращает `false` (`TypeParameterUtils#visitWildcard`) — то есть Kotlin и Java расходились в поведении графа.

Дополнительно `ComponentTemplateHelper.initMap`/`fillMap` и `ComponentDependencyHelper.parseClaim` разыменовывали `KSTypeArgument.type` без проверки на `null` и падали с NPE на том же входе.

### Что сделано

- Обе `hasGenericVariable()` при `type == null` возвращают `false` — как Java-процессор.
- `initMap`/`fillMap` пропускают неразрешимый аргумент вместо NPE.
- `parseClaim` при отсутствии первого аргумента типа падает обратно на claim по целому типу (новый хелпер `claimOfWholeType`).

### Покрытие тестами

Синтетические тесты для star projection и raw-супертипа **не добавлены сознательно**: написанные варианты проходили и с фиксом, и без него — воспроизвести путь через `AbstractSymbolProcessorTest` не удалось, потому что для этого нужен именно Java-класс в classpath, а не Kotlin-исходник. Тест, который зелёный в обоих случаях, даёт ложную уверенность, поэтому он удалён, и это оговорено в сообщении коммита. Фикс проверен интеграционно на трёх gRPC-модулях.

### Затронутые модули `kora-examples`

`examples/kotlin/kora-kotlin-grpc-server`, `kora-kotlin-grpc-client`, `guides/kotlin/kora-kotlin-guide-grpc-server-app` — после фикса компилируются.

---

## PR 13 — `fix(openapi-generator): generate the Kotlin basic auth config as a data class`

- **Ветка:** `fix/openapi-kotlin-security-config-data-class` (локальная, не отправлена)
- **Коммит:** `b2238931d`
- **Модуль фреймворка:** `openapi/openapi-generator`

### Постановка задачи

`ClientSecuritySchemaGenerator.basicAuthConfig` генерировал обычный `class`, а `@ConfigSource` в Kotlin требует `data class` — конфигурационному процессору нужны `componentN`. Kotlin-клиент с basic-авторизацией не собирался, Java-аналог собирался (в Java `record` генерировался корректно).

### Что сделано

`.addModifiers(KModifier.DATA)` в `basicAuthConfig`.

### Покрытие тестами

Тест генератора на спецификации с `basicAuth`: сгенерированный конфиг объявлен как `data class`. Без фикса падает.

### Затронутые модули `kora-examples`

`examples/kotlin/kora-kotlin-openapi-generator-http-client`.

---

## PR 14 — `fix(test-junit5): return the init lock permits when graph initialization fails`

- **Ветка:** `fix/test-junit5-graph-init-lock-leak` (локальная, не отправлена)
- **Коммит:** `aa82d0c3a`
- **Модуль фреймворка:** `test/test-junit5`

### Постановка задачи

`TestGraph.initialize()` захватывал разрешения общего семафора, вызывал `initGraph()` и освобождал их **следующей строкой**, без `finally`. Любая упавшая инициализация графа удерживала их навсегда, и каждый последующий `@KoraAppTest` в той же JVM вставал в `acquireUninterruptibly()`. Прогон не завершался и не падал — он висел до таймаута CI, показывая только самую первую ошибку.

Найдено при миграции `kora-examples`: один неправильно сконфигурированный модуль превращал 30-секундный прогон в 22-минутный, и настоящие причины остальных падений были не видны.

### Что сделано

Обе точки захвата освобождают разрешения в `finally`.

### Покрытие тестами

`TestGraphLockLeakTest` дёргает `TestGraph` напрямую с `Config`, у которого `setup()` бросает `IOException`, и проверяет, что число доступных разрешений не изменилось. Без фикса — 63 из 64 для ветки без системных свойств и 0 из 64 для ветки с ними.

Попытка инициализации выполняется в daemon-потоке с `join(30_000)`: тестируемый режим отказа — это **блокировка**, а не исключение, поэтому прямой вызов повесил бы сборку вместо того, чтобы её уронить. В `finally` утёкшие разрешения возвращаются, иначе первый тест отравил бы второй.

### Влияние на совместимость

Чистое исправление. Ранее работавшие прогоны не затрагиваются.

---

## PR 15 — `fix(openapi-generator): stop requesting a converter for a multipart file part`

- **Ветка:** `fix/openapi-server-multipart-file-unused-converter` (локальная, не отправлена)
- **Коммит:** `dc32c616e`
- **Модуль фреймворка:** `openapi/openapi-generator` (режимы `java-server` и `kotlin-server`)

### Постановка задачи

Для операции с `multipart/form-data` и полем `type: string, format: binary` генератор добавлял в конструктор request-маппера `HttpServerParameterReader<byte[]>` (Kotlin — `<ByteArray>`), но в теле `apply` его не использовал: бинарная часть отдаётся как `FormMultipart.FormPart` напрямую. Такого компонента во фреймворке нет и быть не должно — конвертер читает `String`. Приложение не собиралось.

Дефект существовал в фикстурах генератора (`petstoreV3_form.yaml`), но тесты его не ловили: они только компилируют сгенерированный код, а он компилируется — ломается лишь построение графа `@KoraApp`.

### Что сделано

В обоих генераторах цикл объявления конвертеров пропускает `formParam.isFile`, но только когда тело действительно пойдёт по multipart-ветке:

```java
// url-encoded wins when an operation declares both, same as the apply() body below
var multipartBody = multipartForm && !urlEncodedForm;
```

Привязка к тому же решению, что и выбор ветки в `apply`, а не к одному лишь `multipartForm`: если операция объявляет оба content-type, тело идёт по `mapUrlEncoded`, а она конвертер вызывает.

### Покрытие тестами

`HttpServerJavaOpenapiTest#multipartFileFormParamDoesNotAskForAConverterItNeverUses` и одноимённый в `HttpServerKotlinOpenapiTest`. На существующей фикстуре проверяют, что оба multipart-маппера не объявляют `HttpServerParameterReader`, по-прежнему присваивают `_part`, а url-encoded маппер конвертеры сохранил. Без фикса оба падают.

### Влияние на совместимость

Меняется сигнатура сгенерированного конструктора — но только для случая, который до сих пор не собирался вовсе.

### Затронутые модули `kora-examples`

`guides/java/kora-java-guide-openapi-http-server-advanced-app`, `guides/kotlin/kora-kotlin-guide-openapi-http-server-advanced-app`.
