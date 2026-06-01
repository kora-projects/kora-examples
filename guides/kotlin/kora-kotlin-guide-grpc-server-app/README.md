# Kora Guide Kotlin gRPC Server App

This module is the runnable Kotlin/Gradle companion application for
the [gRPC Server guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/grpc-server.md). It demonstrates
protobuf-first service contracts, generated gRPC server bindings, and a Kora component implementation behind the
service.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/grpc-server)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/grpc-server)

## Kora Modules Covered

- gRPC
  Server: [EN](https://kora-projects.github.io/kora-docs/en/documentation/grpc-server/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/grpc-server/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-server-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-server-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-server-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-server-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-server-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-server-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-grpc-server-app:test
```

## Generated Sources and Artifacts

- gRPC stubs are generated from `src/main/proto` during the Gradle build.
- `distTar` produces `build/distributions/application.tar`.


