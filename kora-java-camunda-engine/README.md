# Kora Java Database JDBC

Пример модуля Database JDBC в Kora.

В примере использовались модули:
- [Jdbc Database](https://kora-projects.github.io/kora-docs/ru/documentation/database-jdbc)

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
docker build -t kora-java-camunda-engine .
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
