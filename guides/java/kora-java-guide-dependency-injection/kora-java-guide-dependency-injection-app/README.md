# Kora Guide Dependency Injection App

This module is the runnable Java/Gradle companion application for the [Dependency Injection guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/dependency-injection.md). It demonstrates a multi-module dependency graph where components are split across application, library, common, and submodule boundaries.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/dependency-injection)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/dependency-injection)

## Kora Modules Covered
- Compile-time dependency injection container: [EN](https://kora-projects.github.io/kora-docs/en/documentation/container/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/container/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:classes
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


