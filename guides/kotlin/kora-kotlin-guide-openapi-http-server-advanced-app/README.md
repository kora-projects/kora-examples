# Kora Guide Kotlin OpenAPI HTTP Server Advanced App

This module is the runnable Kotlin/Gradle companion application for
the [OpenAPI HTTP Server Advanced guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/openapi-http-server-advanced.md).
It demonstrates more advanced OpenAPI server generation scenarios with generated interfaces, validation, exception
handling, and richer endpoint behavior.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/openapi-http-server-advanced)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/openapi-http-server-advanced)

## Kora Modules Covered

- OpenAPI
  Codegen: [EN](https://kora-projects.github.io/kora-docs/en/documentation/openapi-codegen/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-codegen/).
- OpenAPI
  Management: [EN](https://kora-projects.github.io/kora-docs/en/documentation/openapi-management/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/openapi-management/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-advanced-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-advanced-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-advanced-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-advanced-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-advanced-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-advanced-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-openapi-http-server-advanced-app:test
```

## Generated Sources and Artifacts

- OpenAPI-generated sources are created under `build/generated` during `classes` and `testClasses`.
- `distTar` produces `build/distributions/application.tar`.


