# Kora Guide gRPC Client Advanced App

This module is the runnable Java/Gradle companion application for the [gRPC Client Advanced guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/grpc-client-advanced.md). It extends gRPC client usage with more explicit generated client behavior, error handling, and advanced request patterns.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/grpc-client-advanced)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/grpc-client-advanced)

## Kora Modules Covered
- gRPC Client: [EN](https://kora-projects.github.io/kora-docs/en/documentation/grpc-client/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/grpc-client/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-grpc-client-advanced-app:classes
./gradlew :guides:java:kora-java-guide-grpc-client-advanced-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-grpc-client-advanced-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-grpc-client-advanced-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-grpc-client-advanced-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-grpc-client-advanced-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-grpc-client-advanced-app:test
```

## Generated Sources and Artifacts

- gRPC stubs are generated from `src/main/proto` during the Gradle build.
- `distTar` produces `build/distributions/application.tar`.


