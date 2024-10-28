# Kora Java GraalVM CRUD Vertx Service

Пример сервиса реализованного на Kora с упрощенным HTTP [CRUD](https://github.com/swagger-api/swagger-petstore) API
и использованием [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/),
в качестве базы данных выступает Postgres, используется кэш Caffeine, а также другие модули которые использовались бы в реальном приложении в бою.

В примере использовались модули:
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)
- [HTTP Server OpenAPI Generation](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/)
- [Probes](https://kora-projects.github.io/kora-docs/ru/documentation/probes/)
- [Metrics](https://kora-projects.github.io/kora-docs/ru/documentation/metrics/)
- [Database Vertx](https://kora-projects.github.io/kora-docs/ru/documentation/database-vertx/)
- [JSON](https://kora-projects.github.io/kora-docs/ru/documentation/json/)
- [Resilient](https://kora-projects.github.io/kora-docs/ru/documentation/resilient/)
- [Validation](https://kora-projects.github.io/kora-docs/ru/documentation/validation/)
- [Cache Caffeine](https://kora-projects.github.io/kora-docs/ru/documentation/cache/#caffeine)

Скомпилирован с помощью [GraalVM](https://www.graalvm.org/release-notes/JDK_21/)

## Build

Собрать артефакт:

```shell
./gradlew shadowJar
docker build -t kora-java-graalvm-crud-vertx .
```

### Generate

Сгенерировать API для HTTP Server:
```shell
./gradlew openApiGenerateHttpServer
```

## Run

Запустить локально:
```shell
./gradlew run
```

## Test

Тесты используют [Testcontainers](https://java.testcontainers.org/), требуется [Docker](https://docs.docker.com/engine/install/) окружение для запуска тестов или аналогичные контейнерные окружения ([colima](https://github.com/abiosoft/colima) / итп)

Протестировать локально:
```shell
./gradlew test
```
