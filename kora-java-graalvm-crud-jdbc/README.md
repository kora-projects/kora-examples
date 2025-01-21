# Kora Java GraalVM CRUD JDBC Service

Пример сервиса реализованного на Kora с упрощенным HTTP [CRUD](https://github.com/swagger-api/swagger-petstore) API
и использованием [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/),
в качестве базы данных выступает Postgres, используется кэш Caffeine, а также другие модули которые использовались бы в реальном приложении в бою.

В примере использовались модули:
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)
- [HTTP Server OpenAPI Generation](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/)
- [Probes](https://kora-projects.github.io/kora-docs/ru/documentation/probes/)
- [Metrics](https://kora-projects.github.io/kora-docs/ru/documentation/metrics/)
- [Database JDBC](https://kora-projects.github.io/kora-docs/ru/documentation/database-jdbc/)
- [JSON](https://kora-projects.github.io/kora-docs/ru/documentation/json/)
- [Resilient](https://kora-projects.github.io/kora-docs/ru/documentation/resilient/)
- [Validation](https://kora-projects.github.io/kora-docs/ru/documentation/validation/)
- [Cache Caffeine](https://kora-projects.github.io/kora-docs/ru/documentation/cache/#caffeine)

Скомпилирован с помощью [GraalVM](https://www.graalvm.org/release-notes/JDK_21/)

## Build

Собрать классы:

```shell
./gradlew classes
```

Собрать Java артефакт:

```shell
./gradlew shadowJar
```

### Generate

Сгенерировать API для HTTP Server:
```shell
./gradlew openApiGenerateHttpServer
```

### Image

Собрать образ приложения:
```shell
docker build -t kora-java-graalvm-crud-jdbc .
```

Получить GraalVM Native Image артефакт:

```shell
docker run --rm --entrypoint cat kora-java-graalvm-crud-jdbc /opt/app/application > application
```

## Migration

Миграции вызываются с помощью Flyway Gradle Plugin:
```shell
./gradlew flywayMigrate
```

## Run

Запустить Java локально:
```shell
./gradlew run
```

## Run Docker-Compose

Требуется сначала собрать артефакт.

Запустить как docker-compose:
```shell
docker-compose up
```

## Test

Тесты используют [Testcontainers](https://java.testcontainers.org/), требуется [Docker](https://docs.docker.com/engine/install/) окружение для запуска тестов или аналогичные контейнерные окружения ([colima](https://github.com/abiosoft/colima) / итп)

Протестировать локально:
```shell
./gradlew test
```
