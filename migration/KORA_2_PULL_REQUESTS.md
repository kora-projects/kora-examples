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

## Обнаружено, но не исправлено

`Cassandra-репозиторий с CompletableFuture<T> генерирует некомпилируемый код` — подробности, репродукция и предлагаемый фикс в `KORA_2_FRAMEWORK_ISSUES.md`. Фикс требует изменения генерации внутри обёртки `observe(...).call(...)` и отдельного теста в `CassandraResultsTest`; в рамках текущего прохода не делался, модуль помечен `BLOCKED_BY_FRAMEWORK_BUG`.
