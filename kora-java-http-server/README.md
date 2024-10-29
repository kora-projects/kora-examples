# Kora Java HTTP Server

Пример модуля HTTP Server в Kora.

В примере использовались модули:
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)
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
