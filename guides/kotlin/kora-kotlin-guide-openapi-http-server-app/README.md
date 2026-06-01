# Kora Guide Kotlin OpenAPI HTTP Server App

This module is the runnable Kotlin/Gradle companion application for
the [OpenAPI HTTP Server guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/openapi-http-server.md). It
demonstrates OpenAPI-driven server generation and how generated delegates connect an API contract to Kora components.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/openapi-http-server)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/openapi-http-server)

## Kora Modules Covered

- OpenAPI
  Codegen: [EN](https://kora-projects.github.io/kora-docs/en/documentation/openapi-codegen/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/).
- OpenAPI
  Management: [EN](https://kora-projects.github.io/kora-docs/en/documentation/openapi-management/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-management/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-app:test
```

## Generated Sources and Artifacts

- OpenAPI-generated sources are created under `build/generated` during `classes` and `testClasses`.
- `distTar` produces `build/distributions/application.tar`.


