# Kora Java Database R2DBC

Пример модуля Database R2DBC в Kora.

В примере использовались модули:
- [R2dbc Database](https://kora-projects.github.io/kora-docs/ru/documentation/database-r2dbc)

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
