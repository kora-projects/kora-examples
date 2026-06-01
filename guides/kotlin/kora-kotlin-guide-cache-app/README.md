# Kora Guide Kotlin Cache App

This module is the runnable Kotlin/Gradle companion application for
the [Cache guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/cache.md). It demonstrates declarative caching with
generated aspects, cache annotations, key handling, and a service method that becomes cache-aware through Kora code
generation.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/cache)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/cache)

## Kora Modules Covered

-
Cache: [EN](https://kora-projects.github.io/kora-docs/en/documentation/cache/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/cache/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-cache-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-cache-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-cache-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


