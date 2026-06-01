# Kora Guide Kotlin Validation App

This module is the runnable Kotlin/Gradle companion application for
the [Validation guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/validation.md). It demonstrates validation
annotations, generated validation aspects, custom constraints, and HTTP boundary validation.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/validation)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/validation)

## Kora Modules Covered

-
Validation: [EN](https://kora-projects.github.io/kora-docs/en/documentation/validation/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/validation/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-validation-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-validation-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-validation-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-validation-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-validation-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-validation-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-validation-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


