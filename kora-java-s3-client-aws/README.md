# Kora Java S3 AWS

Пример модуля S3 AWS в Kora.

В примере использовались модули:
- [S3 AWS](https://kora-projects.github.io/kora-docs/ru/documentation/s3-client/#aws)

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
