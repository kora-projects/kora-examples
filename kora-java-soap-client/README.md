# Kora Java Soap Client

Пример модуля SOAP Client в Kora.

В примере использовались модули:
- [Soap Client](https://kora-projects.github.io/kora-docs/ru/documentation/soap-client/)

## Build

Собрать артефакт:

```shell
./gradlew distTar
```

### Generate

Сгенерировать API для SOAP:
```shell
./gradlew wsdl2java
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
