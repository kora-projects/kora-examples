# Kora Guide gRPC Server Advanced App

This module is the runnable Java/Gradle companion application for the [gRPC Server Advanced guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/grpc-server-advanced.md). It extends gRPC server behavior with richer service logic, errors, generated code inspection, and advanced server patterns.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/grpc-server-advanced)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/grpc-server-advanced)

## Kora Modules Covered
- gRPC Server: [EN](https://kora-projects.github.io/kora-docs/en/documentation/grpc-server/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/grpc-server/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-grpc-server-advanced-app:classes
./gradlew :guides:java:kora-java-guide-grpc-server-advanced-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-grpc-server-advanced-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-grpc-server-advanced-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-grpc-server-advanced-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-grpc-server-advanced-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-grpc-server-advanced-app:test
```

## Generated Sources and Artifacts

- gRPC stubs are generated from `src/main/proto` during the Gradle build.
- `distTar` produces `build/distributions/application.tar`.


