# Kora Guide Kotlin gRPC Client App

This module is the runnable Kotlin/Gradle companion application for
the [gRPC Client guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/grpc-client.md). It demonstrates generated
protobuf stubs, Kora gRPC client wiring, and typed calls from one component into a gRPC service.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/grpc-client)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/grpc-client)

## Kora Modules Covered

- gRPC
  Client: [EN](https://kora-projects.github.io/kora-docs/en/documentation/grpc-client/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/grpc-client/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-client-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-client-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-client-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-client-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-client-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-client-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-client-app:test
```

## Generated Sources and Artifacts

- gRPC stubs are generated from `src/main/proto` during the Gradle build.
- `distTar` produces `build/distributions/application.tar`.


