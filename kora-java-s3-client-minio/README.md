# Kora Java S3 Minio

Пример модуля S3 Minio в Kora.

В примере использовались модули:
- [S3 Minio](https://kora-projects.github.io/kora-docs/ru/documentation/s3-client/#minio)

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
