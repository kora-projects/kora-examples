# Kora 1.x → 2.0 — статус миграции модулей

Живой документ. Обновляется после каждого мигрированного или заблокированного модуля.

## Как получены данные

Статусы ниже — не оценка, а результат реального прогона:

```shell
export JAVA_HOME=<JDK 25>
./gradlew clean --continue
./gradlew classes testClasses --continue --no-build-cache
```

- Ветка примеров: `migration/2.0`, фреймворк: локальный `../kora` @ `master`, опубликован как `io.koraframework:*:2.0.0-SNAPSHOT` в Maven Local.
- **Gradle-процесс обязан идти на JDK 25.** На JDK 21 сборка падает ещё на конфигурации: buildscript-зависимость `io.koraframework:openapi-generator` требует JVM 25. Toolchain-настройки модулей этого не покрывают — нужен именно JDK, которым запущен Gradle.
- **Первый прогон после смены пакетов делать с `--no-build-cache` и после `clean`.** Задачи-генераторы (OpenAPI, protobuf, wsdl2java) не удаляют предыдущий вывод, а ключ кеша не учитывает смену `apiPackage`/`modelPackage`. Без этого компиляция валится сотнями фантомных `package ru.tinkoff.kora.* does not exist` из устаревших сгенерированных файлов, которых уже нет в исходниках.

## Сводка

| Статус | Модулей |
|---|---|
| `MIGRATION_IN_PROGRESS` — компилируется (main + test), тесты ещё не прогонялись | 50 |
| `READY_FOR_MIGRATION` — нужна семантическая правка кода | 55 |
| `BLOCKED_BY_FRAMEWORK_BUG` — падает KSP-процессор Kora 2.0 | 22 |
| `BLOCKED_BY_REMOVED_FUNCTIONALITY` — интеграция удалена в 2.0 (R2DBC, Vert.x) | 6 |
| **Всего Gradle-модулей с исходниками** | **133** |

Ни один модуль пока не может носить статус `MIGRATED`: по критериям миграции для этого нужны пройденные тесты, а тестовый прогон ещё не выполнялся. Единственное исключение — эталонный `examples/java/kora-java-crud`, он собирается и его тесты проходили ранее; он перепроверяется отдельно.

## Фактический прогресс (обновляется по ходу)

Таблица ниже отражает первый полный прогон. После него статусы изменились так:

| Модуль | Статус | Проверено |
|---|---|---|
| `examples/java/kora-java-http-server` | `MIGRATED` | компиляция + 16/16 тестов (blackbox в Docker) |
| `examples/java/kora-java-http-client` | `MIGRATED` | компиляция + 9/9 тестов (mockserver) |
| `examples/java/kora-java-grpc-client` | `MIGRATION_IN_PROGRESS` | компиляция main + test; тесты не гонялись |
| `examples/java/kora-java-database-jdbc` | `MIGRATION_IN_PROGRESS` | компиляция main + test; прогон тестов не завершён |
| `examples/java/kora-java-kafka` | `MIGRATION_IN_PROGRESS` | компиляция main + test; требовал фикса фреймворка |
| `examples/java/kora-java-database-cassandra` | `BLOCKED_BY_FRAMEWORK_BUG` | дефект генератора для `CompletableFuture<T>` |

Два дефекта фреймворка, блокировавшие `http-client` и `kafka`, исправлены локально в `../kora` с регрессионными тестами — см. `KORA_2_PULL_REQUESTS.md`.

## Классы оставшихся проблем

1. **Краши KSP-процессоров Kora 2.0** (22 модуля) — процессор падает с внутренним исключением вместо диагностики: `NoSuchElementException: No TypeParameter found for index T`, `KaInvalidLifetimeOwnerAccessException`, `NullPointerException`, `IllegalStateException: Required value was null`, `JacksonIOException: Stream closed`, `ClassCastException: String → KSType`. Часть из них — реакция на ещё не мигрированный код (например `ClassCastException` в resilient-модулях вызван строковым `@CircuitBreaker("pet")`), часть проявляется на корректном коде. Разбор — в `KORA_2_FRAMEWORK_ISSUES.md`.
2. **Строковый resilient-API** — `@CircuitBreaker("pet")` / `@Retry("pet")` / `@Timeout("pet")` заменены типизированными spec-интерфейсами.
3. **S3-клиент** — пакеты `io.koraframework.s3.client.model` / `.annotation` и аннотация `@S3` изменены (самый крупный по числу ошибок модуль: `kora-java-s3-client-aws`, 162 ошибки).
4. **Мапперы и интерцепторы как DI-компоненты** — классы, на которые ссылаются `@Mapping` / `@InterceptWith`, теперь инжектятся генерируемым модулем и обязаны быть `@Component`.
5. **JSpecify-нуллабельность** — `@Nullable` как type-use аннотация ставится не везде, где стояла `jakarta.annotation.@Nullable`.
6. **OpenAPI-генерация** — сгенерированные интерфейсы API оказываются package-private (`PetApi is not public …`), несовпадение сигнатур делегатов, `JsonNullable`.
7. **`io.koraframework.config.common.extractor`** — пакет исчез, нужны новые типы извлечения конфигурации.
8. **gRPC** — остатки `ru.tinkoff.grpc.client` в конфигурации proto-генерации.

## Таблица модулей

Колонка «Ошибок» — количество разобранных сообщений компилятора/процессора в последнем прогоне.

| Модуль | Язык | Статус | Ошибок | Детали |
|---|---|---|---|---|
| `examples/graalvm/kora-java-graalvm-crud-cassandra` | Java | `READY_FOR_MIGRATION` | 68 | 38× cannot find symbol; 10× method does not override or implement a method from a supe |
| `examples/graalvm/kora-java-graalvm-crud-jdbc` | Java | `READY_FOR_MIGRATION` | 50 | 38× cannot find symbol; 8× incompatible types: String cannot be converted to Class<?  |
| `examples/graalvm/kora-java-graalvm-crud-r2dbc` | Java | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | 0 | не входит в сборку 2.0 (интеграция удалена) |
| `examples/graalvm/kora-java-graalvm-crud-vertx` | Java | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | 0 | не входит в сборку 2.0 (интеграция удалена) |
| `examples/graalvm/kora-java-graalvm-kafka` | Java | `READY_FOR_MIGRATION` | 4 | 4× cannot find symbol |
| `examples/java/kora-java-cache-caffeine` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-cache-redis` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-camunda-engine` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-camunda-zeebe-worker` | Java | `READY_FOR_MIGRATION` | 16 | 8× cannot find symbol; 4× package io.camunda.zeebe.client.api.response does not exis |
| `examples/java/kora-java-config-hocon` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-config-yaml` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-crud` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-common` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-pet-api` | Java | `READY_FOR_MIGRATION` | 26 | 14× cannot find symbol; 8× incompatible types: String cannot be converted to Class<?  |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-vet-api` | Java | `READY_FOR_MIGRATION` | 28 | 18× cannot find symbol; 10× incompatible types: String cannot be converted to Class<?  |
| `examples/java/kora-java-database-cassandra` | Java | `READY_FOR_MIGRATION` | 14 | 6× cannot find symbol; 4× type annotation @org.jspecify.annotations.Nullable is not  |
| `examples/java/kora-java-database-jdbc` | Java | `READY_FOR_MIGRATION` | 18 | 14× cannot find symbol; 4× type annotation @org.jspecify.annotations.Nullable is not  |
| `examples/java/kora-java-database-r2dbc` | Java | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | 0 | не входит в сборку 2.0 (интеграция удалена) |
| `examples/java/kora-java-database-vertx` | Java | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | 0 | не входит в сборку 2.0 (интеграция удалена) |
| `examples/java/kora-java-grpc-client` | Java | `READY_FOR_MIGRATION` | 2 | 1× package ru.tinkoff.grpc.client does not exist; 1× cannot find symbol |
| `examples/java/kora-java-grpc-server` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-helloworld` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-http-client` | Java | `READY_FOR_MIGRATION` | 13 | 13× cannot find symbol |
| `examples/java/kora-java-http-server` | Java | `READY_FOR_MIGRATION` | 0 | сборка упала без разобранных сообщений |
| `examples/java/kora-java-kafka` | Java | `READY_FOR_MIGRATION` | 8 | 2× Kafka records listener method has unsupported parameter:; 2× Kafka record listener method has unsupported parameter: |
| `examples/java/kora-java-openapi-generator-http-client` | Java | `READY_FOR_MIGRATION` | 12 | 10× PetApi is not public in io.koraframework.example.openapi.p; 2× Dependency has non-reference type: |
| `examples/java/kora-java-openapi-generator-http-server` | Java | `READY_FOR_MIGRATION` | 48 | 14× method does not override or implement a method from a supe; 10× cannot find symbol |
| `examples/java/kora-java-resilient` | Java | `READY_FOR_MIGRATION` | 0 | сборка упала без разобранных сообщений |
| `examples/java/kora-java-s3-client-aws` | Java | `READY_FOR_MIGRATION` | 162 | 74× package S3 does not exist; 68× cannot find symbol |
| `examples/java/kora-java-s3-client-minio` | Java | `READY_FOR_MIGRATION` | 0 | сборка упала без разобранных сообщений |
| `examples/java/kora-java-scheduling-jdk` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-scheduling-quartz` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-soap-client` | Java | `READY_FOR_MIGRATION` | 0 | сборка упала без разобранных сообщений |
| `examples/java/kora-java-telemetry` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/java/kora-java-validation` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-cache-caffeine` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-cache-redis` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `examples/kotlin/kora-kotlin-camunda-engine` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] tools.jackson.core.exc.JacksonIOException: Stream clos |
| `examples/kotlin/kora-kotlin-camunda-zeebe-worker` | Kotlin | `READY_FOR_MIGRATION` | 17 | 13× Unresolved reference 'X'.; 3× Overload resolution ambiguity between candidates: |
| `examples/kotlin/kora-kotlin-config-hocon` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-config-yaml` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-crud` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] ksp.org.jetbrains.kotlin.analysis.api.lifetime.KaInval |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-app` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-common` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-pet-api` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] ksp.org.jetbrains.kotlin.analysis.api.lifetime.KaInval |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-vet-api` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] ksp.org.jetbrains.kotlin.analysis.api.lifetime.KaInval |
| `examples/kotlin/kora-kotlin-database-cassandra` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `examples/kotlin/kora-kotlin-database-jdbc` | Kotlin | `READY_FOR_MIGRATION` | 10 | 7× Unresolved reference 'X'.; 1× Annotation argument must be a compile-time constant. |
| `examples/kotlin/kora-kotlin-database-r2dbc` | Kotlin | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | 0 | не входит в сборку 2.0 (интеграция удалена) |
| `examples/kotlin/kora-kotlin-database-vertx` | Kotlin | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | 0 | не входит в сборку 2.0 (интеграция удалена) |
| `examples/kotlin/kora-kotlin-grpc-client` | Kotlin | `READY_FOR_MIGRATION` | 3 | 3× Unresolved reference 'X'. |
| `examples/kotlin/kora-kotlin-grpc-server` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.lang.NullPointerException |
| `examples/kotlin/kora-kotlin-helloworld` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `examples/kotlin/kora-kotlin-http-client` | Kotlin | `READY_FOR_MIGRATION` | 2 | 2× [ksp] /Users/dsudomoin/IdeaProjects/kora-examples/examples |
| `examples/kotlin/kora-kotlin-http-server` | Kotlin | `READY_FOR_MIGRATION` | 77 | 72× Unresolved reference 'X'.; 2× Class 'X' is not abstract and does not implement abstract  |
| `examples/kotlin/kora-kotlin-kafka` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.lang.IllegalStateException: Required value was nu |
| `examples/kotlin/kora-kotlin-openapi-generator-http-client` | Kotlin | `READY_FOR_MIGRATION` | 1 | 1× [ksp] /Users/dsudomoin/IdeaProjects/kora-examples/examples |
| `examples/kotlin/kora-kotlin-openapi-generator-http-server` | Kotlin | `READY_FOR_MIGRATION` | 31 | 14× Cannot infer type for type parameter 'X'. Specify it expli; 9× Unresolved reference 'X'. |
| `examples/kotlin/kora-kotlin-resilient` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.lang.ClassCastException: class java.lang.String c |
| `examples/kotlin/kora-kotlin-s3-client-aws` | Kotlin | `READY_FOR_MIGRATION` | 1 | 1× [ksp] /Users/dsudomoin/IdeaProjects/kora-examples/examples |
| `examples/kotlin/kora-kotlin-s3-client-minio` | Kotlin | `READY_FOR_MIGRATION` | 0 | сборка упала без разобранных сообщений |
| `examples/kotlin/kora-kotlin-scheduling-jdk` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-scheduling-quartz` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-soap-client` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-telemetry` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `examples/kotlin/kora-kotlin-validation` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-cache-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-cache-multi-level-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-config-hocon-app` | Java | `READY_FOR_MIGRATION` | 8 | 4× cannot find symbol; 2× package io.koraframework.config.common.extractor does not  |
| `guides/java/kora-java-guide-config-yaml-app` | Java | `READY_FOR_MIGRATION` | 3 | 2× cannot find symbol; 1× package io.koraframework.config.common.extractor does not  |
| `guides/java/kora-java-guide-database-cassandra-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-database-jdbc-advanced-app` | Java | `READY_FOR_MIGRATION` | 4 | 4× cannot find symbol |
| `guides/java/kora-java-guide-database-jdbc-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-common` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-lib` | Java | `READY_FOR_MIGRATION` | 4 | 2× package io.koraframework.config.common.extractor does not ; 2× cannot find symbol |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-submodule` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-dependency-injection-introduction-app` | Java | `READY_FOR_MIGRATION` | 0 | сборка упала без разобранных сообщений |
| `guides/java/kora-java-guide-getting-started-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-grpc-client-advanced-app` | Java | `READY_FOR_MIGRATION` | 4 | 2× package ru.tinkoff.grpc.client does not exist; 2× cannot find symbol |
| `guides/java/kora-java-guide-grpc-client-app` | Java | `READY_FOR_MIGRATION` | 4 | 2× package ru.tinkoff.grpc.client does not exist; 2× cannot find symbol |
| `guides/java/kora-java-guide-grpc-server-advanced-app` | Java | `READY_FOR_MIGRATION` | 0 | сборка упала без разобранных сообщений |
| `guides/java/kora-java-guide-grpc-server-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-http-client-advanced-app` | Java | `READY_FOR_MIGRATION` | 14 | 12× cannot find symbol; 2× No component found for dependency: |
| `guides/java/kora-java-guide-http-client-app` | Java | `READY_FOR_MIGRATION` | 2 | 2× cannot find symbol |
| `guides/java/kora-java-guide-http-server-advanced-app` | Java | `READY_FOR_MIGRATION` | 6 | 6× cannot find symbol |
| `guides/java/kora-java-guide-http-server-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-json-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-messaging-kafka-app` | Java | `READY_FOR_MIGRATION` | 2 | 2× No component found for dependency: |
| `guides/java/kora-java-guide-observability-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-openapi-http-client-app` | Java | `READY_FOR_MIGRATION` | 8 | 6× UsersApi is not public in io.koraframework.guide.openapi.h; 2× Dependency has non-reference type: |
| `guides/java/kora-java-guide-openapi-http-server-advanced-app` | Java | `READY_FOR_MIGRATION` | 18 | 10× cannot find symbol; 2× wrong number of type arguments; required 2 |
| `guides/java/kora-java-guide-openapi-http-server-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-resilient-app` | Java | `READY_FOR_MIGRATION` | 26 | 16× cannot find symbol; 4× incompatible types: String cannot be converted to Class<?  |
| `guides/java/kora-java-guide-s3-app` | Java | `READY_FOR_MIGRATION` | 48 | 24× cannot find symbol; 10× package io.koraframework.s3.client.model does not exist |
| `guides/java/kora-java-guide-testing-black-box-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-testing-integration-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-testing-junit-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/java/kora-java-guide-validation-app` | Java | `READY_FOR_MIGRATION` | 2 | 2× No component found for dependency: |
| `guides/kotlin/kora-kotlin-guide-cache-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-cache-multi-level-app` | Kotlin | `READY_FOR_MIGRATION` | 2 | 2× Unresolved reference 'X'. |
| `guides/kotlin/kora-kotlin-guide-config-hocon-app` | Kotlin | `READY_FOR_MIGRATION` | 6 | 6× Unresolved reference 'X'. |
| `guides/kotlin/kora-kotlin-guide-config-yaml-app` | Kotlin | `READY_FOR_MIGRATION` | 6 | 6× Unresolved reference 'X'. |
| `guides/kotlin/kora-kotlin-guide-database-cassandra-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] tools.jackson.core.exc.JacksonIOException: Stream clos |
| `guides/kotlin/kora-kotlin-guide-database-jdbc-advanced-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-database-jdbc-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-app` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-common` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-lib` | Kotlin | `READY_FOR_MIGRATION` | 3 | 3× Unresolved reference 'X'. |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-submodule` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-dependency-injection-introduction-app` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-getting-started-app` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-grpc-client-advanced-app` | Kotlin | `READY_FOR_MIGRATION` | 3 | 3× Unresolved reference 'X'. |
| `guides/kotlin/kora-kotlin-guide-grpc-client-app` | Kotlin | `READY_FOR_MIGRATION` | 3 | 3× Unresolved reference 'X'. |
| `guides/kotlin/kora-kotlin-guide-grpc-server-advanced-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.lang.NullPointerException |
| `guides/kotlin/kora-kotlin-guide-grpc-server-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.lang.NullPointerException |
| `guides/kotlin/kora-kotlin-guide-http-client-advanced-app` | Kotlin | `READY_FOR_MIGRATION` | 26 | 10× Unresolved reference 'X'.; 3× Class 'X' is not abstract and does not implement abstract  |
| `guides/kotlin/kora-kotlin-guide-http-client-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-http-server-advanced-app` | Kotlin | `READY_FOR_MIGRATION` | 33 | 19× Unresolved reference 'X'.; 3× Cannot infer type for value parameter 'X'. Specify it expl |
| `guides/kotlin/kora-kotlin-guide-http-server-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-json-app` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-messaging-kafka-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-observability-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-openapi-http-client-app` | Kotlin | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-openapi-http-server-advanced-app` | Kotlin | `READY_FOR_MIGRATION` | 18 | 7× Unresolved reference 'X'.; 3× Cannot infer type for type parameter 'X'. Specify it expli |
| `guides/kotlin/kora-kotlin-guide-openapi-http-server-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] java.util.NoSuchElementException: No TypeParameter fou |
| `guides/kotlin/kora-kotlin-guide-resilient-app` | Kotlin | `BLOCKED_BY_FRAMEWORK_BUG` | 1 | KSP-процессор падает: [ksp] tools.jackson.core.exc.JacksonIOException: Stream clos |
| `guides/kotlin/kora-kotlin-guide-s3-app` | Kotlin | `READY_FOR_MIGRATION` | 1 | 1× [ksp] /Users/dsudomoin/IdeaProjects/kora-examples/guides/k |
| `guides/kotlin/kora-kotlin-guide-testing-black-box-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-testing-integration-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-testing-junit-app` | Java | `MIGRATION_IN_PROGRESS` | 0 | компилируется (main+test); тесты ещё не прогонялись |
| `guides/kotlin/kora-kotlin-guide-validation-app` | Kotlin | `READY_FOR_MIGRATION` | 1 | 1× [ksp] /Users/dsudomoin/IdeaProjects/kora-examples/guides/k |


## Легенда статусов

Используются статусы из регламента миграции: `ALREADY_MIGRATED`, `READY_FOR_MIGRATION`, `MIGRATION_IN_PROGRESS`, `MIGRATED`, `PARTIALLY_MIGRATED`, `BLOCKED_BY_REMOVED_FUNCTIONALITY`, `BLOCKED_BY_FRAMEWORK_BUG`, `BLOCKED_BY_GRAALVM_BUILD`, `REQUIRES_REDESIGN`, `REQUIRES_INVESTIGATION`, `REQUIRES_GRAALVM_INVESTIGATION`, `NOT_APPLICABLE`.

GraalVM-модули дополнительно потребуют статуса по native-сборке: `native-image` в системе не установлен, поэтому native-часть намеренно отложена и будет отмечена отдельно (`REQUIRES_GRAALVM_INVESTIGATION`) после стабилизации JVM-части.
