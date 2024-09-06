# Kora Java Telemetry

Пример модулей телеметрии, метрик и проб в Kora.

В примере использовались модули:
- [Metrics](https://kora-projects.github.io/kora-docs/ru/documentation/metrics/)
- [Probes](https://kora-projects.github.io/kora-docs/ru/documentation/probes/)
- [Tracing](https://kora-projects.github.io/kora-docs/ru/documentation/tracing/)

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

