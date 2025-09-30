# Kora Java Camunda Zeebe

Пример модуля Camunda Zeebe в Kora.

В примере использовались модули:
- [Camunda Zeebe](https://kora-projects.github.io/kora-docs/ru/documentation/camunda8-worker/)

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
docker build -t kora-java-camunda-zeebe-worker .
```

## Run

Перед запуском локально требуется запустить Zeebe оркестратор.

Запустить локально:
```shell
./gradlew run
```

## Run Docker-Compose

Требуется сначала собрать артефакт:

```shell
./gradlew distTar
```

Запустить как docker-compose:
```shell
docker-compose up
```

## Test

Тесты используют [Testcontainers](https://java.testcontainers.org/), требуется [Docker](https://docs.docker.com/engine/install/) окружение для запуска тестов или аналогичные контейнерные окружения ([colima](https://github.com/abiosoft/colima) / итп)

Протестировать локально:
```shell
./gradlew test
```
