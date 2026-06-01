# Kora Guide JSON App

This module is the runnable Java/Gradle companion application for the [JSON guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/json.md). It demonstrates explicit JSON DTOs, generated readers and writers, and how request/response objects become serialization-safe at compile time.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/json)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/json)

## Kora Modules Covered
- JSON: [EN](https://kora-projects.github.io/kora-docs/en/documentation/json/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/json/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-json-app:classes
./gradlew :guides:java:kora-java-guide-json-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-json-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-json-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-json-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-json-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-json-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


