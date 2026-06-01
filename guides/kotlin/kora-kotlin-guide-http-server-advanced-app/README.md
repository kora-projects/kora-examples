# Kora Guide Kotlin HTTP Server Advanced App

This module is the runnable Kotlin/Gradle companion application for
the [HTTP Server Advanced guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/http-server-advanced.md). It expands
manual HTTP server usage with request handlers, interceptors, custom exception handling, form processing, and
lower-level HTTP server extension points.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/http-server-advanced)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/http-server-advanced)

## Kora Modules Covered

- HTTP
  Server: [EN](https://kora-projects.github.io/kora-docs/en/documentation/http-server/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-http-server-advanced-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-http-server-advanced-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-http-server-advanced-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-http-server-advanced-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-http-server-advanced-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-http-server-advanced-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-http-server-advanced-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


