# Kora Guide Observability App

This module is the runnable Java/Gradle companion application for the [Observability guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/observability.md). It demonstrates logs, metrics, tracing, probes, and the Kora telemetry modules used to observe an application from the first run.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/observability)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/observability)

## Kora Modules Covered
- Metrics: [EN](https://kora-projects.github.io/kora-docs/en/documentation/metrics/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/metrics/).
- Tracing: [EN](https://kora-projects.github.io/kora-docs/en/documentation/tracing/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/tracing/).
- Logging SLF4J: [EN](https://kora-projects.github.io/kora-docs/en/documentation/logging-slf4j/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/logging-slf4j/).
- Probes: [EN](https://kora-projects.github.io/kora-docs/en/documentation/probes/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/probes/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-observability-app:classes
./gradlew :guides:java:kora-java-guide-observability-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-observability-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-observability-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-observability-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-observability-app:run
```

## Local Infrastructure

Start companion infrastructure when running the application manually against local services:

```shell
docker compose -f guides/java/kora-java-guide-observability-app/docker-compose.yml up -d
```

Stop it when finished:

```shell
docker compose -f guides/java/kora-java-guide-observability-app/docker-compose.yml down
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-observability-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


