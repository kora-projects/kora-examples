# Kora Guide Kotlin Testing Integration App

This module is the runnable Kotlin/Gradle companion application for
the [Testing Integration guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/testing-integration.md). It
demonstrates integration testing with a real Kora graph, external dependency configuration, and application-level
verification.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/testing-integration)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/testing-integration)

## Kora Modules Covered

- JUnit 5
  testing: [EN](https://kora-projects.github.io/kora-docs/en/documentation/junit5/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/junit5/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-testing-integration-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-testing-integration-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-testing-integration-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-testing-integration-app:testClasses
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-testing-integration-app:test
```


