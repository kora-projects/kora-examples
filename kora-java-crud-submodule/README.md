# Kora Java Submodule CRUD Service

Пример сервиса реализованного на Kora с упрощенным HTTP [CRUD](https://github.com/swagger-api/swagger-petstore) API
использующий разделение сервиса на проектные модули по функциональности, 
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

Основной функционал и точка входа в приложение находится в модуле `kora-java-crud-submodule-app`.

## Build

Собрать классы:

```shell
./gradlew classes
```

Собрать артефакт:

```shell
./gradlew distTar
```

### Generate

Сгенерировать API для HTTP Server:
```shell
./gradlew openApiGenerateHttpServer
```

### Image

Собрать образ приложения:
```shell
docker build -t kora-java-crud-submodule .
```

## Migration

Миграции вызываются с помощью Flyway Gradle Plugin:
```shell
./gradlew flywayMigrate
```

## Run

Перед запуском локально требуется запустить базу Postgres и накатить миграции.

Запустить локально:
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
