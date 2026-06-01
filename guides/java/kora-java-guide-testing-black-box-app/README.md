# Kora Guide Testing Black Box App

This module is the runnable Java/Gradle companion application for the [Testing Black Box guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/testing-black-box.md). It demonstrates black-box testing through real application boundaries so HTTP behavior, configuration, and generated wiring are verified together.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/testing-black-box)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/testing-black-box)

## Kora Modules Covered
- JUnit 5 testing: [EN](https://kora-projects.github.io/kora-docs/en/documentation/junit5/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/junit5/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-testing-black-box-app:classes
./gradlew :guides:java:kora-java-guide-testing-black-box-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-testing-black-box-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-testing-black-box-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-testing-black-box-app:distTar
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-testing-black-box-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


