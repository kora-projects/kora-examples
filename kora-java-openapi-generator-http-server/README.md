# Kora Java OpenAPI Generator HTTP Server

Пример модуля OpenAPI Generator в Kora.

В примере использовались модули:
- [OpenAPI Generation](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/)
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)
- [JSON](https://kora-projects.github.io/kora-docs/ru/documentation/json/)

## Build

Собрать артефакт:

```shell
./gradlew distTar
```

### Generate

Сгенерировать API для PetV2.yml:
```shell
./gradlew openApiGenerate
```

Сгенерировать API для PetV3.yml:
```shell
./gradlew openApiGeneratePetV3
```

### Run

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
