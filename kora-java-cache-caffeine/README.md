# Kora Java Cache Caffeine

Пример модуля Cache Caffeine в Kora.

В примере использовались модули:
- [Caffeine Cache](https://kora-projects.github.io/kora-docs/ru/documentation/cache/#caffeine)

## Build

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
