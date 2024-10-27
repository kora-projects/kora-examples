# Kora Java GraalVM Kafka

Пример сервиса реализованного на Kora с использованием Kafka 
и [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/).

В примере использовались модули:
- [Kafka](https://kora-projects.github.io/kora-docs/ru/documentation/kafka/)
- [JSON](https://kora-projects.github.io/kora-docs/ru/documentation/json/)

Скомпилирован с помощью [GraalVM](https://www.graalvm.org/release-notes/JDK_21/)

## Build

Собрать артефакт:

```shell
./gradlew shadowJar
docker build -t kora-java-graalvm-kafka .
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
