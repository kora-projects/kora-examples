# Kora Guide HTTP Client Advanced App

This module is the runnable Java/Gradle companion application for the [HTTP Client Advanced guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/http-client-advanced.md). It demonstrates advanced HTTP client usage, including interceptors, manual calls through Kora HttpClient, and explicit request composition.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/http-client-advanced)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/http-client-advanced)

## Kora Modules Covered
- HTTP Client: [EN](https://kora-projects.github.io/kora-docs/en/documentation/http-client/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/http-client/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-http-client-advanced-app:classes
./gradlew :guides:java:kora-java-guide-http-client-advanced-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-http-client-advanced-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-http-client-advanced-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-http-client-advanced-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-http-client-advanced-app:run
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-http-client-advanced-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


