# Kora 1.x → 2.0 — статус миграции модулей

Живой документ. Обновляется после каждого мигрированного или заблокированного модуля.

## Как получены данные

Статусы ниже — не оценка, а результат реального прогона тестов каждого модуля:

```shell
export JAVA_HOME=<JDK 25>
./gradlew <все модули>:test --max-workers=1 --continue
```

- Ветка примеров: `migration/2.0`, фреймворк: локальный `../kora` @ `integration/migration-fixes`,
  опубликован как `io.koraframework:*:2.0.0-SNAPSHOT` в Maven Local.
- **Gradle-процесс обязан идти на JDK 25.** На JDK 21 сборка падает ещё на конфигурации:
  buildscript-зависимость `io.koraframework:openapi-generator` требует JVM 25. Toolchain-настройки
  модулей этого не покрывают — нужен именно JDK, которым запущен Gradle.
- **Первый прогон после смены пакетов делать с `--no-build-cache` и после `clean`.** Задачи-генераторы
  (OpenAPI, protobuf, wsdl2java) не удаляют предыдущий вывод, а ключ кеша не учитывает смену
  `apiPackage`/`modelPackage`. Без этого компиляция валится сотнями фантомных
  `package ru.tinkoff.kora.* does not exist` из устаревших сгенерированных файлов.
- **`--max-workers=1` обязателен для достоверных цифр.** При параллельной сборке KSP-процессоры
  дают гонку (`JacksonIOException: Stream closed`) — на одном воркере таких падений ноль.
- Колонка «Тесты» — фактические `PASSED`/`FAILED` из лога прогона, а не XML-отчёты: часть модулей
  их отключает (`junitXml.required = false`).

## Сводка

| Показатель | Значение |
|---|---|
| Gradle-модулей в `settings.gradle` | 127 |
| Модулей с тестами | 116 |
| Тестов пройдено | **556** |
| Тестов упало | **0** |
| Модулей со статусом `MIGRATED` | 127 |
| Модулей со статусом `MIGRATION_IN_PROGRESS` | 0 |
| GraalVM native-образов собрано и проверено в работе | 3 |
| Дефектов фреймворка исправлено | 25 |

Прогон: `./gradlew <116 задач>:test --max-workers=1 --continue`, 10 мин 53 с.

Единственное падение в этом прогоне — `:examples:java:kora-java-crud:compileJava` — воспроизводится
только при инкрементальной сборке: процессор базы данных читает интерфейс репозитория из
class-файла и видит `:arg0` вместо имён параметров. С `--rerun` модуль собирается и даёт 9/9.
Это описано в обоих руководствах (§1.5/§1.6) как ловушка инкрементальной компиляции, а не как
дефект примера.

## Что сделано

Мигрированы все модули репозитория — Java, Kotlin и GraalVM. Для трёх модулей `examples/graalvm/*`
native-образ собран на GraalVM CE 25.0.4 и проверен в работе: бинарь запущен против реальных
зависимостей (Postgres, Kafka, Scylla + Redis), отвечает на `/system/readiness`, отдаёт метрики и
обслуживает свой сценарий (CRUD либо обработку сообщения).

По ходу миграции найдено и исправлено **двадцать пять дефектов самого фреймворка** — каждый
отдельной веткой от `master`. Все двадцать пять отправлены в upstream как отдельные pull request'ы
[#791–#815](https://github.com/kora-projects/kora/pulls?q=is%3Apr+author%3Adsudomoin);
разбор каждого — в `KORA_2_PULL_REQUESTS.md`, причины и опровергнутые гипотезы —
в `KORA_2_FRAMEWORK_ISSUES.md`.

Самые тяжёлые из них не проявлялись как ошибка компиляции:

| Дефект | Почему был незаметен |
|---|---|
| ни один спан не экспортируется (`OpentelemetryContext.with` теряет обёртку) | приложение работает, метрики идут, трассировка молча пуста |
| эндпоинт метрик всегда «Metric Scraper disabled» | HTTP 200, тело-заглушка |
| java-генератор OpenAPI ставит `to = minimum` в `@Range` | тесты не отправляли значений выше минимума |
| gRPC-приложение завершается сразу после старта | контейнер выходит с кодом 0, как при штатном завершении |
| `TestGraph` теряет permit семафора при упавшей инициализации | прогон висит вместо падения, видна только первая ошибка |
| метаданные трёх модулей названы `reflection-config.json` | файл с таким именем native-image не читает вовсе — регистрации никогда не применялись |
| Undertow в native падает с «No XNIO provider found» | провайдер в образе есть; настоящую ошибку глотает `catch (Throwable)` в XNIO |

## Закрытые ранее открытые вопросы

**Порядок схем авторизации в `openapi-generator-http-client`.** Записывался как вопрос к дизайну
фреймворка — оказался ошибкой примера: заглушки bearer/oAuth возвращали константы и перебивали
apiKey. Провайдер без учётных данных обязан возвращать `null`. Заодно выяснилось, что секцию
безопасности генератор читает по `clientConfigPrefix`, а не по `securityConfigPrefix`.

**`BlackBoxTests` в `kora-java-crud-submodule`.** Контейнер выходил с кодом 255, не дождавшись
`/system/readiness`. Причина — ключ `openapi.management.file`, который в 2.0 называется `files`
и молча не читался. 7/7.

**«Для `HttpResponseEntity<T>` выбирается не тот шаблонный маппер».** Опровергнуто экспериментом:
это каскад от дефекта multipart-конвертера. `GraphBuilder` форкает весь оставшийся граф на каждого
кандидата шаблона, поэтому одна нерешаемая зависимость роняет все форки и печатает ошибку про
несвязанный компонент.

**«gRPC требует решения по дизайну фреймворка».** Тоже опровергнуто: `XnioLifecycle` уже держит
non-daemon-поток ровно для этого и прямо это комментирует — контракт установлен, gRPC-сервер просто
перестал его выполнять после перехода на виртуальные потоки.

## Замечание по именованию S3-модулей

`kora-java-s3-client-minio` и `kora-kotlin-s3-client-minio` в 2.0 демонстрируют уже не SDK Minio
(реализации `s3-client-minio` в 2.0 нет), а декларативный клиент `s3-client-kora`, который гоняется
против Minio как S3-совместимого хранилища. Каталоги специально **не переименованы**: это решение
о структуре репозитория, а не часть миграции, и оно задело бы четыре модуля примеров плюс ссылки
в README. Расхождение объяснено в README самих модулей. Если решение будет принято, естественные
имена — `kora-java-s3-client-kora` / `kora-kotlin-s3-client-kora`.

## Что осталось

- Диагностика `GraphBuilder` при провале всех форков шаблона — дефект подтверждён и описан,
  но не исправлен: нужен дизайн вывода (сводка по всем кандидатам вместо одного случайного).
- `ConfigWatcher` читает узел графа до его инициализации — стектрейс при каждом старте и молчаливо
  неработающее слежение за конфигом. Воспроизводится и на JVM, и в native. Не исправлен: нужно
  решение о контракте, а не глушение исключения.

## Модули

Колонки: язык, рантайм, основные интеграции Kora, статус, результат компиляции, результат
кодогенерации, результат тестов, результат native-сборки.

Про колонку «Native» у `examples/graalvm/*`: образ собран `nativeCompile` на GraalVM CE 25.0.4
и запущен против реальных зависимостей — `/system/readiness` отвечает `OK`, `/metrics` отдаёт
метрики, сценарий модуля отрабатывает (CRUD либо обработка сообщения).

Сверх того, у всех трёх модулей тот же образ собирается **внутри Docker** по `Dockerfile`
(`ghcr.io/graalvm/native-image-community:25`) и проверяется `BlackBoxTests` против реальных
зависимостей — Postgres, Kafka, Scylla + Redis.

**Две ловушки тестового окружения, которые стоит знать.** `BlackBoxTests` сассандры требуют
свободного Docker: при параллельно поднятых четырёх контейнерах Scylla не успевает стартовать
и расширение отваливается по таймауту. А kafka-модуль ждал готовности контейнера по строке в логе —
а строка не появлялась, потому что логи терялись из-за дефекта `KoraAsyncAppender`
([#816](https://github.com/kora-projects/kora/pull/816)). После его фикса тест проходит практически
в исходном виде: расширение по-прежнему управляет брокером и топиками, а изменилось два места —
готовность контейнера ждётся по `/system/readiness` (формулировки логов Kora — не контракт), и убраны
отладочные `System.out.println`, оставшиеся от миграции. Подмена порта на `:9093` — не костыль:
брокер Testcontainers объявляет хосту `:9092`, а внутрисетевой listener — на `:9093`.

| Модуль | Язык | Рантайм | Интеграции Kora | Статус | Компиляция | Кодоген | Тесты | Native |
|---|---|---|---|---|---|---|---|---|
| `examples/graalvm/kora-java-graalvm-crud-cassandra` | Java | JVM + GraalVM | openapi-gen, http-server, cassandra, metrics, json, validation, cache-redis, resilient, config-hocon, openapi-mgmt, logback | `MIGRATED` | OK | OK | 7/7 (вкл. `BlackBoxTests` на native-образе) | OK, 76 МиБ |
| `examples/graalvm/kora-java-graalvm-crud-jdbc` | Java | JVM + GraalVM | openapi-gen, http-server, jdbc, metrics, json, validation, cache-caffeine, resilient, config-hocon, openapi-mgmt, logback | `MIGRATED` | OK | OK | 7/7 (вкл. `BlackBoxTests` на native-образе) | OK, 51 МиБ |
| `examples/graalvm/kora-java-graalvm-kafka` | Java | JVM + GraalVM | http-server, kafka, json, config-yaml, metrics, logback | `MIGRATED` | OK | — | 1/1 (`BlackBoxTests` на native-образе) | OK, 80 МиБ |
| `examples/java/kora-java-cache-caffeine` | Java | JVM | cache-caffeine, logback, config-hocon | `MIGRATED` | OK | OK | 10/10 | — |
| `examples/java/kora-java-cache-redis` | Java | JVM | cache-redis, logback, config-hocon | `MIGRATED` | OK | OK | 9/9 | — |
| `examples/java/kora-java-camunda-engine` | Java | JVM | http-server, camunda-engine, json, jdbc, logback, config-hocon | `MIGRATED` | OK | OK | 3/3 | — |
| `examples/java/kora-java-camunda-zeebe-worker` | Java | JVM | zeebe, scheduling, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/java/kora-java-config-hocon` | Java | JVM | logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/java/kora-java-config-yaml` | Java | JVM | logback, config-yaml | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/java/kora-java-crud` | Java | JVM | openapi-gen, http-server, jdbc, metrics, json, validation, cache-caffeine, resilient, config-hocon, openapi-mgmt, logback | `MIGRATED` | OK | OK | 9/9 | — |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-app` | Java | JVM | openapi-gen, http-server, config-hocon, logback, json, metrics, validation, openapi-mgmt | `MIGRATED` | OK | OK | 7/7 | — |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-common` | Java | JVM | — | `MIGRATED` | OK | OK | нет тестов | — |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-pet-api` | Java | JVM | jdbc, cache-caffeine, resilient, config-hocon | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/java/kora-java-crud-submodule/kora-java-crud-submodule-vet-api` | Java | JVM | jdbc, cache-caffeine, resilient, config-hocon | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/java/kora-java-database-cassandra` | Java | JVM | cassandra, logback, config-hocon | `MIGRATED` | OK | OK | 9/9 | — |
| `examples/java/kora-java-database-jdbc` | Java | JVM | jdbc, logback, config-hocon | `MIGRATED` | OK | OK | 22/22 | — |
| `examples/java/kora-java-grpc-client` | Java | JVM | grpc-client, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/java/kora-java-grpc-server` | Java | JVM | grpc-server, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/java/kora-java-helloworld` | Java | JVM | http-server, json, config-hocon, logback | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/java/kora-java-http-client` | Java | JVM | http-client, json, logback, config-hocon | `MIGRATED` | OK | OK | 9/9 | — |
| `examples/java/kora-java-http-server` | Java | JVM | http-server, json, validation, logback, config-hocon, http-client | `MIGRATED` | OK | OK | 16/16 | — |
| `examples/java/kora-java-kafka` | Java | JVM | kafka, json, logback, config-hocon | `MIGRATED` | OK | OK | 24/24 | — |
| `examples/java/kora-java-openapi-generator-http-client` | Java | JVM | openapi-gen, validation, http-client, json, logback, config-hocon | `MIGRATED` | OK | OK | 4/4 | — |
| `examples/java/kora-java-openapi-generator-http-server` | Java | JVM | openapi-gen, validation, http-server, json, logback, config-hocon | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/java/kora-java-resilient` | Java | JVM | resilient, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/java/kora-java-s3-client-aws` | Java | JVM | s3-aws, http-client, logback, config-hocon | `MIGRATED` | OK | OK | 5/5 | — |
| `examples/java/kora-java-s3-client-minio` | Java | JVM | s3-kora, http-client, logback, config-hocon | `MIGRATED` | OK | OK | 6/6 | — |
| `examples/java/kora-java-scheduling-jdk` | Java | JVM | scheduling, logback, config-hocon | `MIGRATED` | OK | OK | 4/4 | — |
| `examples/java/kora-java-scheduling-quartz` | Java | JVM | scheduling-quartz, logback, config-hocon | `MIGRATED` | OK | OK | 3/3 | — |
| `examples/java/kora-java-soap-client` | Java | JVM | json, http-client, soap, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/java/kora-java-telemetry` | Java | JVM | metrics, tracing, http-server, logback, config-hocon | `MIGRATED` | OK | OK | 4/4 | — |
| `examples/java/kora-java-validation` | Java | JVM | validation, logback, config-hocon | `MIGRATED` | OK | OK | 5/5 | — |
| `examples/kotlin/kora-kotlin-cache-caffeine` | Kotlin | JVM | cache-caffeine, config-hocon, logback | `MIGRATED` | OK | OK | 10/10 | — |
| `examples/kotlin/kora-kotlin-cache-redis` | Kotlin | JVM | cache-redis, config-hocon, logback | `MIGRATED` | OK | OK | 9/9 | — |
| `examples/kotlin/kora-kotlin-camunda-engine` | Kotlin | JVM | http-server, camunda-engine, json, jdbc, logback, config-hocon | `MIGRATED` | OK | OK | 3/3 | — |
| `examples/kotlin/kora-kotlin-camunda-zeebe-worker` | Kotlin | JVM | zeebe, scheduling, logback, config-hocon, json | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/kotlin/kora-kotlin-config-hocon` | Kotlin | JVM | config-hocon, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/kotlin/kora-kotlin-config-yaml` | Kotlin | JVM | config-yaml, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/kotlin/kora-kotlin-crud` | Kotlin | JVM | openapi-gen, http-server, http-client, jdbc, metrics, json, validation, cache-caffeine, resilient, config-hocon, openapi-mgmt, logback | `MIGRATED` | OK | OK | 9/9 | — |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-app` | Kotlin | JVM | openapi-gen, http-server, config-hocon, logback, json, metrics, validation, openapi-mgmt | `MIGRATED` | OK | OK | 7/7 | — |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-common` | Kotlin | JVM | — | `MIGRATED` | OK | OK | нет тестов | — |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-pet-api` | Kotlin | JVM | jdbc, cache-caffeine, resilient, config-hocon | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/kotlin/kora-kotlin-crud-submodule/kora-kotlin-crud-submodule-vet-api` | Kotlin | JVM | jdbc, cache-caffeine, resilient, config-hocon | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/kotlin/kora-kotlin-database-cassandra` | Kotlin | JVM | cassandra, config-hocon, logback | `MIGRATED` | OK | OK | 8/8 | — |
| `examples/kotlin/kora-kotlin-database-jdbc` | Kotlin | JVM | jdbc, json, logback, config-hocon | `MIGRATED` | OK | OK | 18/18 | — |
| `examples/kotlin/kora-kotlin-grpc-client` | Kotlin | JVM | grpc-client, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/kotlin/kora-kotlin-grpc-server` | Kotlin | JVM | grpc-server, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/kotlin/kora-kotlin-helloworld` | Kotlin | JVM | http-server, json, config-hocon, logback | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/kotlin/kora-kotlin-http-client` | Kotlin | JVM | http-client, json, logback, config-hocon | `MIGRATED` | OK | OK | 11/11 | — |
| `examples/kotlin/kora-kotlin-http-server` | Kotlin | JVM | http-server, json, validation, config-hocon, logback, http-client | `MIGRATED` | OK | OK | 17/17 | — |
| `examples/kotlin/kora-kotlin-kafka` | Kotlin | JVM | kafka, json, logback, config-hocon | `MIGRATED` | OK | OK | 24/24 | — |
| `examples/kotlin/kora-kotlin-openapi-generator-http-client` | Kotlin | JVM | openapi-gen, validation, http-client, json, logback, config-hocon | `MIGRATED` | OK | OK | 4/4 | — |
| `examples/kotlin/kora-kotlin-openapi-generator-http-server` | Kotlin | JVM | openapi-gen, validation, http-server, json, logback, config-hocon | `MIGRATED` | OK | OK | 2/2 | — |
| `examples/kotlin/kora-kotlin-resilient` | Kotlin | JVM | resilient, config-hocon, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/kotlin/kora-kotlin-s3-client-aws` | Kotlin | JVM | s3-aws, http-client, logback, config-hocon | `MIGRATED` | OK | OK | 5/5 | — |
| `examples/kotlin/kora-kotlin-s3-client-minio` | Kotlin | JVM | s3-kora, http-client, logback, config-hocon | `MIGRATED` | OK | OK | 6/6 | — |
| `examples/kotlin/kora-kotlin-scheduling-jdk` | Kotlin | JVM | scheduling, config-hocon, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `examples/kotlin/kora-kotlin-scheduling-quartz` | Kotlin | JVM | scheduling-quartz, config-hocon, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `examples/kotlin/kora-kotlin-soap-client` | Kotlin | JVM | json, http-client, soap, logback, config-hocon | `MIGRATED` | OK | OK | 1/1 | — |
| `examples/kotlin/kora-kotlin-telemetry` | Kotlin | JVM | http-server, json, metrics, tracing, config-hocon, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `examples/kotlin/kora-kotlin-validation` | Kotlin | JVM | validation, config-hocon, logback | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/java/kora-java-guide-cache-app` | Java | JVM | cache-caffeine, config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/java/kora-java-guide-cache-multi-level-app` | Java | JVM | cache-caffeine, cache-redis, config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/java/kora-java-guide-config-hocon-app` | Java | JVM | config-hocon, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/java/kora-java-guide-config-yaml-app` | Java | JVM | config-yaml, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/java/kora-java-guide-database-cassandra-app` | Java | JVM | config-hocon, cassandra, http-server, json, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/java/kora-java-guide-database-jdbc-advanced-app` | Java | JVM | config-hocon, flyway, jdbc, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/java/kora-java-guide-database-jdbc-app` | Java | JVM | config-hocon, flyway, jdbc, http-server, json, logback | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/java/kora-java-guide-dependency-injection-introduction-app` | Java | JVM | config-hocon, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-app` | Java | JVM | config-hocon, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-common` | Java | JVM | — | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-lib` | Java | JVM | config-common | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/java/kora-java-guide-dependency-injection/kora-java-guide-dependency-injection-submodule` | Java | JVM | — | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/java/kora-java-guide-getting-started-app` | Java | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/java/kora-java-guide-grpc-client-advanced-app` | Java | JVM | config-hocon, grpc-client, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/java/kora-java-guide-grpc-client-app` | Java | JVM | config-hocon, grpc-client, http-server, json, logback | `MIGRATED` | OK | OK | 6/6 | — |
| `guides/java/kora-java-guide-grpc-server-advanced-app` | Java | JVM | config-hocon, grpc-server, logback | `MIGRATED` | OK | OK | 7/7 | — |
| `guides/java/kora-java-guide-grpc-server-app` | Java | JVM | config-hocon, grpc-server, logback | `MIGRATED` | OK | OK | 6/6 | — |
| `guides/java/kora-java-guide-http-client-advanced-app` | Java | JVM | config-hocon, http-client, http-server, json, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/java/kora-java-guide-http-client-app` | Java | JVM | config-hocon, http-client, http-server, json, logback | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/java/kora-java-guide-http-server-advanced-app` | Java | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/java/kora-java-guide-http-server-app` | Java | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/java/kora-java-guide-json-app` | Java | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/java/kora-java-guide-messaging-kafka-app` | Java | JVM | config-hocon, http-server, json, kafka, logback | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/java/kora-java-guide-observability-app` | Java | JVM | config-hocon, http-server, json, logback, metrics, tracing | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/java/kora-java-guide-openapi-http-client-app` | Java | JVM | openapi-gen, config-hocon, http-client, http-server, json, logback, validation | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/java/kora-java-guide-openapi-http-server-advanced-app` | Java | JVM | openapi-gen, config-hocon, http-server, json, logback, openapi-mgmt, validation | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/java/kora-java-guide-openapi-http-server-app` | Java | JVM | openapi-gen, config-hocon, http-server, json, logback, openapi-mgmt, validation | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/java/kora-java-guide-resilient-app` | Java | JVM | config-hocon, http-server, json, logback, resilient | `MIGRATED` | OK | OK | 8/8 | — |
| `guides/java/kora-java-guide-s3-app` | Java | JVM | config-hocon, http-client, http-server, json, logback, s3-aws, s3-kora | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/java/kora-java-guide-testing-black-box-app` | Java | JVM | — | `MIGRATED` | OK | OK | 6/6 | — |
| `guides/java/kora-java-guide-testing-integration-app` | Java | JVM | config-hocon, flyway, jdbc, http-client, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/java/kora-java-guide-testing-junit-app` | Java | JVM | http-server | `MIGRATED` | OK | OK | 6/6 | — |
| `guides/java/kora-java-guide-validation-app` | Java | JVM | config-hocon, http-server, json, logback, validation | `MIGRATED` | OK | OK | 13/13 | — |
| `guides/kotlin/kora-kotlin-guide-cache-app` | Kotlin | JVM | cache-caffeine, config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/kotlin/kora-kotlin-guide-cache-multi-level-app` | Kotlin | JVM | cache-caffeine, cache-redis, config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/kotlin/kora-kotlin-guide-config-hocon-app` | Kotlin | JVM | config-hocon, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/kotlin/kora-kotlin-guide-config-yaml-app` | Kotlin | JVM | config-yaml, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/kotlin/kora-kotlin-guide-database-cassandra-app` | Kotlin | JVM | config-hocon, cassandra, http-server, json, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/kotlin/kora-kotlin-guide-database-jdbc-advanced-app` | Kotlin | JVM | config-hocon, flyway, jdbc, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/kotlin/kora-kotlin-guide-database-jdbc-app` | Kotlin | JVM | config-hocon, flyway, jdbc, http-server, json, logback | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/kotlin/kora-kotlin-guide-dependency-injection-introduction-app` | Kotlin | JVM | config-hocon, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-app` | Kotlin | JVM | config-hocon, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-common` | Kotlin | JVM | — | `MIGRATED` | OK | — | нет тестов | — |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-lib` | Kotlin | JVM | config-common | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/kotlin/kora-kotlin-guide-dependency-injection/kora-kotlin-guide-dependency-injection-submodule` | Kotlin | JVM | — | `MIGRATED` | OK | OK | нет тестов | — |
| `guides/kotlin/kora-kotlin-guide-getting-started-app` | Kotlin | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/kotlin/kora-kotlin-guide-grpc-client-advanced-app` | Kotlin | JVM | config-hocon, grpc-client, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/kotlin/kora-kotlin-guide-grpc-client-app` | Kotlin | JVM | config-hocon, grpc-client, http-server, json, logback | `MIGRATED` | OK | OK | 6/6 | — |
| `guides/kotlin/kora-kotlin-guide-grpc-server-advanced-app` | Kotlin | JVM | config-hocon, grpc-server, logback | `MIGRATED` | OK | OK | 7/7 | — |
| `guides/kotlin/kora-kotlin-guide-grpc-server-app` | Kotlin | JVM | config-hocon, grpc-server, logback | `MIGRATED` | OK | OK | 6/6 | — |
| `guides/kotlin/kora-kotlin-guide-http-client-advanced-app` | Kotlin | JVM | config-hocon, http-client, http-server, json, logback | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/kotlin/kora-kotlin-guide-http-client-app` | Kotlin | JVM | config-hocon, http-client, http-server, json, logback | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/kotlin/kora-kotlin-guide-http-server-advanced-app` | Kotlin | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 3/3 | — |
| `guides/kotlin/kora-kotlin-guide-http-server-app` | Kotlin | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/kotlin/kora-kotlin-guide-json-app` | Kotlin | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/kotlin/kora-kotlin-guide-messaging-kafka-app` | Kotlin | JVM | config-hocon, http-server, json, kafka, logback | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/kotlin/kora-kotlin-guide-observability-app` | Kotlin | JVM | config-hocon, http-server, json, logback, metrics, tracing | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/kotlin/kora-kotlin-guide-openapi-http-client-app` | Kotlin | JVM | openapi-gen, config-hocon, http-client, http-server, json, logback, validation | `MIGRATED` | OK | OK | 5/5 | — |
| `guides/kotlin/kora-kotlin-guide-openapi-http-server-advanced-app` | Kotlin | JVM | openapi-gen, config-hocon, http-server, json, logback, openapi-mgmt, validation | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/kotlin/kora-kotlin-guide-openapi-http-server-app` | Kotlin | JVM | openapi-gen, config-hocon, http-server, json, logback, openapi-mgmt, validation | `MIGRATED` | OK | OK | 1/1 | — |
| `guides/kotlin/kora-kotlin-guide-resilient-app` | Kotlin | JVM | config-hocon, http-server, json, logback, resilient | `MIGRATED` | OK | OK | 8/8 | — |
| `guides/kotlin/kora-kotlin-guide-s3-app` | Kotlin | JVM | config-hocon, http-client, http-server, json, logback, s3-aws, s3-kora | `MIGRATED` | OK | OK | 2/2 | — |
| `guides/kotlin/kora-kotlin-guide-testing-black-box-app` | Kotlin | JVM | — | `MIGRATED` | OK | — | 6/6 | — |
| `guides/kotlin/kora-kotlin-guide-testing-integration-app` | Kotlin | JVM | config-hocon, flyway, jdbc, http-client, http-server, json, logback | `MIGRATED` | OK | OK | 4/4 | — |
| `guides/kotlin/kora-kotlin-guide-testing-junit-app` | Kotlin | JVM | config-hocon, http-server, json, logback | `MIGRATED` | OK | OK | 6/6 | — |
| `guides/kotlin/kora-kotlin-guide-validation-app` | Kotlin | JVM | config-hocon, http-server, json, logback, validation | `MIGRATED` | OK | OK | 13/13 | — |

## Легенда статусов

Используются статусы из регламента миграции: `ALREADY_MIGRATED`, `READY_FOR_MIGRATION`,
`MIGRATION_IN_PROGRESS`, `MIGRATED`, `PARTIALLY_MIGRATED`, `BLOCKED_BY_REMOVED_FUNCTIONALITY`,
`BLOCKED_BY_FRAMEWORK_BUG`, `BLOCKED_BY_GRAALVM_BUILD`, `REQUIRES_REDESIGN`,
`REQUIRES_INVESTIGATION`, `REQUIRES_GRAALVM_INVESTIGATION`, `NOT_APPLICABLE`.

Статусов `MIGRATION_IN_PROGRESS`, `BLOCKED_BY_GRAALVM_BUILD` и `REQUIRES_GRAALVM_INVESTIGATION` в таблице
больше нет: все три модуля `examples/graalvm/*` собираются в native-образ и проверены в работе
— как через `nativeCompile` (Gradle-плагин), так и через `Dockerfile` (сборка внутри
`ghcr.io/graalvm/native-image-community:25`), который гоняется тестами `BlackBoxTests`.

Модули на удалённой в 2.0 функциональности (`database-r2dbc`, `database-vertx`) исключены из
`settings.gradle`, но **не удалены** из репозитория и помечены `BLOCKED_BY_REMOVED_FUNCTIONALITY`:

| Модуль | Статус | Причина |
|---|---|---|
| `examples/java/kora-java-database-r2dbc` | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | интеграция R2DBC удалена в 2.0 |
| `examples/java/kora-java-database-vertx` | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | интеграция Vert.x SQL удалена в 2.0 |
| `examples/kotlin/kora-kotlin-database-r2dbc` | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | то же |
| `examples/kotlin/kora-kotlin-database-vertx` | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | то же |
| `examples/graalvm/kora-java-graalvm-crud-r2dbc` | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | то же |
| `examples/graalvm/kora-java-graalvm-crud-vertx` | `BLOCKED_BY_REMOVED_FUNCTIONALITY` | то же |
