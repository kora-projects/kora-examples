# Kora Java Cache Redis

Пример модуля Cache Redis в Kora.

В примере использовались модули:
- [Redis Cache](https://kora-projects.github.io/kora-docs/ru/documentation/cache/#redis)

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
