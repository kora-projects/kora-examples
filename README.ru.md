<p align="center">
  <a href="https://kora-projects.github.io/kora-docs">
    <img src="https://kora-projects.github.io/kora-docs/v2/ru/assets/img/kora-long.png" alt="Kora Framework" width="420">
  </a>
</p>

<h1 align="center">Песочница Kora</h1>

<p align="center">
  <b>Практическая песочница для <a href="https://kora-projects.github.io/kora-docs">фреймворка Kora</a>.</b><br>
  Клонируйте, запускайте любой модуль, дёргайте настоящие эндпоинты, ломайте и смотрите, как работает Kora — на Java и Kotlin.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.koraframework/common"><img src="https://img.shields.io/maven-central/v/io.koraframework/common.svg?label=maven%20central" alt="Maven Central"></a>
  <a href="https://github.com/kora-projects/kora-examples/actions?query=workflow%3A%22Build+Master%22"><img src="https://github.com/kora-projects/kora-examples/workflows/Build%20Master/badge.svg" alt="Build"></a>
  <a href="https://github.com/kora-projects/kora-examples/blob/master/LICENSE"><img src="https://img.shields.io/github/license/kora-projects/kora-examples.svg" alt="License"></a>
</p>

<p align="center">
  <a href="https://github.com/kora-projects/kora">Фреймворк</a> ·
  <a href="https://kora-projects.github.io/kora-docs">Документация</a> ·
  <a href="https://kora-projects.github.io/kora-docs/guides">Ознакомление</a> ·
  <a href="https://github.com/kora-projects/kora-skills">Kora Skills</a>
</p>

> English version: [README.md](README.md)

---

Это песочница для [фреймворка Kora](https://kora-projects.github.io/kora-docs). Каждый модуль здесь — небольшой самостоятельный сервис, который запускает одну часть Kora целиком: настоящий код, настоящая конфигурация, настоящие тесты. Ничего не нужно настраивать: склонируйте репозиторий, запустите модуль, отправьте ему запрос, а затем откройте исходники и что-нибудь измените, чтобы посмотреть, что произойдёт.

Всё есть и на **Java, и на Kotlin** — экспериментируйте на том языке, на котором пишете сервисы.

**Ещё удобнее с нейро-агентом.** Установите наш навык [kora-skills](https://github.com/kora-projects/kora-skills), и ваш нейро-агент будет запускать эти примеры, объяснять, что сгенерировала Kora, и менять их вместе с вами — прямо по официальным руководствам и коду.

## Поиграть за минуту

```bash
git clone https://github.com/kora-projects/kora-examples.git
cd kora-examples

# запустите любой модуль — например, HTTP-сервер
./gradlew :kora-java-http-server:run
```

Затем дёрните его, поменяйте обработчик и перезапустите:

```bash
curl http://localhost:8080/hello
```

Каждый пример — **самостоятельный Gradle-модуль**, поэтому можно собрать или протестировать один в отрыве от остальных:

```bash
./gradlew :kora-java-http-server:build     # собрать один модуль
./gradlew :kora-java-http-server:test      # прогнать его тесты
./gradlew build                            # собрать всё
```

Модули, которым нужна инфраструктура (PostgreSQL, Kafka, Cassandra, S3, …), содержат рядом с кодом `docker-compose.yml` и `Dockerfile` — сначала поднимите зависимости через `docker compose up` внутри модуля. **У каждого модуля есть свой `README.md`** с точными командами, портами и примерами запросов.

> Нужен **JDK 17+**; Gradle wrapper (`./gradlew`) уже в комплекте, больше ставить ничего не нужно.

## С чего начать

- **[Hello World](examples/java/kora-java-helloworld)** — самый маленький сервис на Kora.
- **[Приложение Getting Started](guides/java/kora-java-guide-getting-started-app)** — первый сервис с сопровождением.
- **[Введение во внедрение зависимостей](guides/java/kora-java-guide-dependency-injection-introduction-app)** — как связывается граф во время компиляции.
- **[Полный CRUD-сервис](examples/java/kora-java-crud)** — HTTP + JDBC + тесты вместе, ближе всего к реальному приложению.
- Запустите `./gradlew :<module>:test` на любом модуле и откройте `build/generated/…`, чтобы прочитать код, который Kora сгенерировала для него.

## Две папки

- **[`examples/`](examples)** — самостоятельные демонстрационные сервисы, по одному на модуль Kora. Берите их, чтобы поиграть с одной возможностью в отрыве от остального.
- **[`guides/`](guides)** — приложения-компаньоны к пошаговым [руководствам документации](https://kora-projects.github.io/kora-docs/guides): читаете руководство, запускаете его приложение, следуете за ним.

## Что можно попробовать

В каждой строке — один и тот же запускаемый сервис на обоих языках.

### Основы и старт

| Тема | Java | Kotlin |
|---|---|---|
| Hello World | [Java](examples/java/kora-java-helloworld) | [Kotlin](examples/kotlin/kora-kotlin-helloworld) |
| Полный CRUD-сервис | [Java](examples/java/kora-java-crud) | [Kotlin](examples/kotlin/kora-kotlin-crud) |
| CRUD с `@KoraSubmodule` | [Java](examples/java/kora-java-crud-submodule) | [Kotlin](examples/kotlin/kora-kotlin-crud-submodule) |
| Конфигурация — HOCON | [Java](examples/java/kora-java-config-hocon) | [Kotlin](examples/kotlin/kora-kotlin-config-hocon) |
| Конфигурация — YAML | [Java](examples/java/kora-java-config-yaml) | [Kotlin](examples/kotlin/kora-kotlin-config-yaml) |

### HTTP и API

| Тема | Java | Kotlin |
|---|---|---|
| HTTP-сервер | [Java](examples/java/kora-java-http-server) | [Kotlin](examples/kotlin/kora-kotlin-http-server) |
| HTTP-клиент | [Java](examples/java/kora-java-http-client) | [Kotlin](examples/kotlin/kora-kotlin-http-client) |
| OpenAPI → HTTP-сервер | [Java](examples/java/kora-java-openapi-generator-http-server) | [Kotlin](examples/kotlin/kora-kotlin-openapi-generator-http-server) |
| OpenAPI → HTTP-клиент | [Java](examples/java/kora-java-openapi-generator-http-client) | [Kotlin](examples/kotlin/kora-kotlin-openapi-generator-http-client) |
| SOAP-клиент | [Java](examples/java/kora-java-soap-client) | [Kotlin](examples/kotlin/kora-kotlin-soap-client) |

### Базы данных

| Тема | Java | Kotlin |
|---|---|---|
| JDBC | [Java](examples/java/kora-java-database-jdbc) | [Kotlin](examples/kotlin/kora-kotlin-database-jdbc) |
| R2DBC | [Java](examples/java/kora-java-database-r2dbc) | [Kotlin](examples/kotlin/kora-kotlin-database-r2dbc) |
| Vert.x | [Java](examples/java/kora-java-database-vertx) | [Kotlin](examples/kotlin/kora-kotlin-database-vertx) |
| Cassandra | [Java](examples/java/kora-java-database-cassandra) | [Kotlin](examples/kotlin/kora-kotlin-database-cassandra) |

### Сообщения и RPC

| Тема | Java | Kotlin |
|---|---|---|
| Kafka | [Java](examples/java/kora-java-kafka) | [Kotlin](examples/kotlin/kora-kotlin-kafka) |
| gRPC-сервер | [Java](examples/java/kora-java-grpc-server) | [Kotlin](examples/kotlin/kora-kotlin-grpc-server) |
| gRPC-клиент | [Java](examples/java/kora-java-grpc-client) | [Kotlin](examples/kotlin/kora-kotlin-grpc-client) |
| S3 — AWS SDK | [Java](examples/java/kora-java-s3-client-aws) | [Kotlin](examples/kotlin/kora-kotlin-s3-client-aws) |
| S3 — MinIO | [Java](examples/java/kora-java-s3-client-minio) | [Kotlin](examples/kotlin/kora-kotlin-s3-client-minio) |

### Отказоустойчивость и аспекты

| Тема | Java | Kotlin |
|---|---|---|
| Отказоустойчивость (`@Retry`, `@Timeout`, `@CircuitBreaker`, `@Fallback`) | [Java](examples/java/kora-java-resilient) | [Kotlin](examples/kotlin/kora-kotlin-resilient) |
| Кэш — Caffeine | [Java](examples/java/kora-java-cache-caffeine) | [Kotlin](examples/kotlin/kora-kotlin-cache-caffeine) |
| Кэш — Redis | [Java](examples/java/kora-java-cache-redis) | [Kotlin](examples/kotlin/kora-kotlin-cache-redis) |
| Валидация | [Java](examples/java/kora-java-validation) | [Kotlin](examples/kotlin/kora-kotlin-validation) |
| Планировщик — JDK | [Java](examples/java/kora-java-scheduling-jdk) | [Kotlin](examples/kotlin/kora-kotlin-scheduling-jdk) |
| Планировщик — Quartz | [Java](examples/java/kora-java-scheduling-quartz) | [Kotlin](examples/kotlin/kora-kotlin-scheduling-quartz) |

### Наблюдаемость и процессы

| Тема | Java | Kotlin |
|---|---|---|
| Телеметрия (метрики, трассировка, логи) | [Java](examples/java/kora-java-telemetry) | [Kotlin](examples/kotlin/kora-kotlin-telemetry) |
| Camunda 7 engine | [Java](examples/java/kora-java-camunda-engine) | [Kotlin](examples/kotlin/kora-kotlin-camunda-engine) |
| Camunda 8 Zeebe worker | [Java](examples/java/kora-java-camunda-zeebe-worker) | [Kotlin](examples/kotlin/kora-kotlin-camunda-zeebe-worker) |

### GraalVM Native Image

Сборки в нативный образ (ahead-of-time) — см. [`examples/graalvm`](examples/graalvm):

[CRUD · JDBC](examples/graalvm/kora-java-graalvm-crud-jdbc) · [CRUD · R2DBC](examples/graalvm/kora-java-graalvm-crud-r2dbc) · [CRUD · Vert.x](examples/graalvm/kora-java-graalvm-crud-vertx) · [CRUD · Cassandra](examples/graalvm/kora-java-graalvm-crud-cassandra) · [Kafka](examples/graalvm/kora-java-graalvm-kafka)

## Руководства

В дереве [`guides/`](guides) лежит по одному приложению-компаньону на каждое руководство документации, на [Java](guides/java) и [Kotlin](guides/kotlin) — от старта и внедрения зависимостей до баз данных, HTTP, Kafka, gRPC, отказоустойчивости, валидации, наблюдаемости и трёх стилей тестирования (JUnit, интеграционное, чёрный ящик). Читайте руководство в [документации](https://kora-projects.github.io/kora-docs/guides) и запускайте его приложение рядом.

## Документация и сообщество

- Документация — [kora-docs](https://kora-projects.github.io/kora-docs)
- Руководства — [Ознакомление](https://kora-projects.github.io/kora-docs/guides)
- Исходники фреймворка — [kora-projects/kora](https://github.com/kora-projects/kora)
- Нейро-навык — [kora-skills](https://github.com/kora-projects/kora-skills)

## Лицензия

Распространяется по лицензии [Apache License 2.0](https://github.com/kora-projects/kora-examples/blob/master/LICENSE).
