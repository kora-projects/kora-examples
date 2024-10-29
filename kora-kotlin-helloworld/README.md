# Kora Kotlin Hello World

Пример Kotlin реализованного на Kora сервиса Hello World.

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

### Image

Собрать образ приложения:
```shell
docker build -t kora-kotlin-helloworld .
```

## Run

Запустить локально:
```shell
./gradlew run
```

## Test

Тест использует [Testcontainers](https://java.testcontainers.org/), требуется [Docker](https://docs.docker.com/engine/install/) окружение для запуска тестов или аналогичные контейнерные окружения ([colima](https://github.com/abiosoft/colima) / итп)

Протестировать локально:
```shell
./gradlew test
```
