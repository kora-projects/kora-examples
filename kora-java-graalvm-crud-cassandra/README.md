# Kora Java GraalVM CRUD Cassandra Service

Пример сервиса реализованного на Kora с HTTP [CRUD](https://github.com/swagger-api/swagger-petstore) API,
в качестве базы данных выступает Cassandra, используется кэш Redis, а также другие модули которые использовались бы в реальном приложении в бою.

В примере использовались модули:
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)
- [HTTP Server OpenAPI Generation](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/)
- [Probes](https://kora-projects.github.io/kora-docs/ru/documentation/probes/)
- [Metrics](https://kora-projects.github.io/kora-docs/ru/documentation/metrics/)
- [Database Cassandra](https://kora-projects.github.io/kora-docs/ru/documentation/database-cassandra/)
- [JSON](https://kora-projects.github.io/kora-docs/ru/documentation/json/)
- [Resilient](https://kora-projects.github.io/kora-docs/ru/documentation/resilient/)
- [Validation](https://kora-projects.github.io/kora-docs/ru/documentation/validation/)
- [Cache Redis](https://kora-projects.github.io/kora-docs/ru/documentation/cache/#redis)

Скомпилирован с помощью [GraalVM](https://www.graalvm.org/release-notes/JDK_21/)

## Build

Собрать артефакт:

```shell
./gradlew distTar
docker build -t kora-java-graalvm-crud-cassandra .
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
