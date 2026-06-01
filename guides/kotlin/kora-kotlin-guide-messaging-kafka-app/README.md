# Kora Guide Kotlin Messaging Kafka App

This module is the runnable Kotlin/Gradle companion application for
the [Messaging Kafka guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/messaging-kafka.md). It demonstrates
Kafka producer and consumer components, record serialization, configuration, and local message-flow verification.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/messaging-kafka)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/messaging-kafka)

## Kora Modules Covered

-
Kafka: [EN](https://kora-projects.github.io/kora-docs/en/documentation/kafka/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/kafka/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-messaging-kafka-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-messaging-kafka-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-messaging-kafka-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-messaging-kafka-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-messaging-kafka-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-messaging-kafka-app:run
```

## Local Infrastructure

Start companion infrastructure when running the application manually against local services:

```shell
docker compose -f guides/kotlin/kora-kotlin-guide-messaging-kafka-app/docker-compose.yml up -d
```

Stop it when finished:

```shell
docker compose -f guides/kotlin/kora-kotlin-guide-messaging-kafka-app/docker-compose.yml down
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-messaging-kafka-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


