# Kora Guide Kotlin Cache Multi-Level App

This module is the runnable Kotlin/Gradle companion application for
the [Cache Multi-Level guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/cache-multi-level.md). It demonstrates
a multi-level cache setup where local and remote cache layers cooperate behind Kora cache annotations.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/cache-multi-level)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/cache-multi-level)

## Kora Modules Covered

-
Cache: [EN](https://kora-projects.github.io/kora-docs/en/documentation/cache/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/cache/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-cache-multi-level-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-cache-multi-level-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-multi-level-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-multi-level-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-multi-level-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-multi-level-app:run
```

## Local Infrastructure

Start companion infrastructure when running the application manually against local services:

```shell
docker compose -f guides/kotlin/kora-kotlin-guide-cache-multi-level-app/docker-compose.yml up -d
```

Stop it when finished:

```shell
docker compose -f guides/kotlin/kora-kotlin-guide-cache-multi-level-app/docker-compose.yml down
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-multi-level-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


