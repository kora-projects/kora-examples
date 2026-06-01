# Kora Guide Kotlin Getting Started App

This module is the runnable Kotlin/Gradle companion application for
the [Getting Started guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/getting-started.md). It shows the
smallest runnable Kora application: a compile-time DI graph, application entry point, configuration file, and generated
graph classes that make the framework behavior visible.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/getting-started)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/getting-started)

## Kora Modules Covered

- Compile-time dependency injection
  container: [EN](https://kora-projects.github.io/kora-docs/en/documentation/container/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/container/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-getting-started-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-getting-started-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-getting-started-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-getting-started-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-getting-started-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-getting-started-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-getting-started-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


