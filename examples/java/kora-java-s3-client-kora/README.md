# Kora Java S3 Minio

Пример декларативного S3 клиента Kora, который работает против Minio как S3-совместимого хранилища.

В примере использовались модули:
- `io.koraframework.experimental:s3-client-kora`

> В Kora 1.x декларативный клиент `@S3.Client` был реализован поверх SDK Minio
> (`s3-client-minio`). В Kora 2.0 этой реализации больше нет: декларативный клиент
> собран поверх собственного HTTP клиента Kora и живёт в артефакте `s3-client-kora`.
> Minio здесь остаётся только как хранилище, против которого гоняются тесты.
> Обёртка над AWS SDK — отдельный модуль, см. `kora-java-s3-client-aws`.

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
