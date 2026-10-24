# Kora Kotlin S3 Kora

Пример декларативного S3 клиента Kora, который работает против RustFS как S3-совместимого хранилища.

В примере использовались модули:

- [S3 клиент](https://kora-projects.github.io/kora-docs/ru/documentation/s3-client/)

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

Тесты используют [Testcontainers](https://java.testcontainers.org/),
требуется [Docker](https://docs.docker.com/engine/install/) окружение для запуска тестов или аналогичные контейнерные
окружения ([colima](https://github.com/abiosoft/colima) / итп)

Протестировать локально:

```shell
./gradlew test
```
