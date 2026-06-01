# Kora Guide HTTP Server App

This module is the runnable Java/Gradle companion application for the [HTTP Server guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/http-server.md). It builds a manual Kora HTTP API with controllers, JSON request/response DTOs, repository and service layers, response handling, and application configuration.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/http-server)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/http-server)

## Kora Modules Covered
- HTTP Server: [EN](https://kora-projects.github.io/kora-docs/en/documentation/http-server/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-http-server-app:classes
./gradlew :guides:java:kora-java-guide-http-server-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-http-server-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-http-server-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-http-server-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-http-server-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-http-server-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


