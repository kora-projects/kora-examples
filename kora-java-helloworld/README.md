# Kora Java Hello World

Пример реализованного на Kora сервиса Hello World.

В примере использовались модули:
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)
- [JSON](https://kora-projects.github.io/kora-docs/ru/documentation/json/)

## Build

Собрать артефакт:

```shell
./gradlew shadowJar
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
