# Kora Guide HTTP Client App

This module is the runnable Java/Gradle companion application for the [HTTP Client guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/http-client.md). It demonstrates declarative Kora HTTP clients, typed request/response models, client configuration, and local integration testing.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/http-client)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/http-client)

## Kora Modules Covered
- HTTP Client: [EN](https://kora-projects.github.io/kora-docs/en/documentation/http-client/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/http-client/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-http-client-app:classes
./gradlew :guides:java:kora-java-guide-http-client-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-http-client-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-http-client-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-http-client-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-http-client-app:run
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-http-client-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


