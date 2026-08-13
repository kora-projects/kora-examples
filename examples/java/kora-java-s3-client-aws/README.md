# Kora Java S3 AWS

Пример модуля S3 AWS в Kora.

В примере использовались модули:
- `io.koraframework:s3-client-aws`

> В Kora 2.0 модуль отдаёт в контейнер сам `software.amazon.awssdk.services.s3.S3Client`,
> работа с S3 идёт напрямую через API AWS SDK. Декларативные контракты `@S3.Client`
> в этот артефакт больше не входят — они переехали в `s3-client-kora`,
> см. `kora-java-s3-client-minio`.

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
