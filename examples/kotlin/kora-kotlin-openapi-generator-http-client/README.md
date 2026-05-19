# Kora Java OpenAPI Generator HTTP Client

Пример модуля OpenAPI Generator в Kora.

В примере использовались модули:
- [OpenAPI Generation](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/)
- [HTTP Client](https://kora-projects.github.io/kora-docs/ru/documentation/http-client/)
- [JSON](https://kora-projects.github.io/kora-docs/ru/documentation/json/)

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

Сгенерировать API для PetV2.yml:
```shell
./gradlew openApiGeneratePetV2
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
