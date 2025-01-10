# Kora Java Camunda BPMN

Пример модуля Camunda BPMN & REST в Kora.

В примере использовались модули:
- [Camunda BPMN](https://kora-projects.github.io/kora-docs/ru/documentation/camunda7-bpmn/)
- [Camunda REST](https://kora-projects.github.io/kora-docs/ru/documentation/camunda7-rest/)

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
