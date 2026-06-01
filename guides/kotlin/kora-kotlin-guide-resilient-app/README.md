# Kora Guide Kotlin Resilient App

This module is the runnable Kotlin/Gradle companion application for
the [Resilient guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/resilient.md). It demonstrates retry, circuit
breaker, timeout, and fallback behavior, including generated aspects around service methods.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/resilient)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/resilient)

## Kora Modules Covered

-
Resilient: [EN](https://kora-projects.github.io/kora-docs/en/documentation/resilient/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/resilient/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-resilient-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-resilient-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-resilient-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-resilient-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-resilient-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-resilient-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-resilient-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


