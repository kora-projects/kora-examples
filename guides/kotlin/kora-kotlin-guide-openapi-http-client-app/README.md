# Kora Guide Kotlin OpenAPI HTTP Client App

This module is the runnable Kotlin/Gradle companion application for
the [OpenAPI HTTP Client guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/openapi-http-client.md). It
demonstrates OpenAPI-driven client generation and how generated clients are wired into Kora applications.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/openapi-http-client)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/openapi-http-client)

## Kora Modules Covered

- OpenAPI
  Codegen: [EN](https://kora-projects.github.io/kora-docs/en/documentation/openapi-codegen/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/).
- HTTP
  Client: [EN](https://kora-projects.github.io/kora-docs/en/documentation/http-client/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/http-client/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-client-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-client-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-client-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-client-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-client-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-client-app:run
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-client-app:test
```

## Generated Sources and Artifacts

- OpenAPI-generated sources are created under `build/generated` during `classes` and `testClasses`.
- `distTar` produces `build/distributions/application.tar`.


