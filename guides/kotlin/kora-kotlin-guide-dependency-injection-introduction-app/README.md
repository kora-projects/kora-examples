# Kora Guide Kotlin Dependency Injection Introduction App

This module is the runnable Kotlin/Gradle companion application for
the [Dependency Injection Introduction guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/dependency-injection-introduction.md).
It demonstrates the basic dependency injection vocabulary used by Kora: components, modules, constructor injection,
graph generation, and the way dependencies are resolved at compile time.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/dependency-injection-introduction)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/dependency-injection-introduction)

## Kora Modules Covered

- Compile-time dependency injection
  container: [EN](https://kora-projects.github.io/kora-docs/en/documentation/container/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/container/).

## Run Checks

```bash
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection-introduction-app:classes
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection-introduction-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps
the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection-introduction-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection-introduction-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection-introduction-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection-introduction-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection-introduction-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


