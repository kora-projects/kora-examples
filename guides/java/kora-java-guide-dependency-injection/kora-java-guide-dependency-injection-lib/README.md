# Kora Guide Dependency Injection Library Module

This module is part of the Java multi-module companion project for the [Dependency Injection guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/dependency-injection.md). It contains reusable components that are imported into the application graph from a library-style module.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/dependency-injection)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/dependency-injection)

## Kora Modules Covered
- Compile-time dependency injection container: [EN](https://kora-projects.github.io/kora-docs/en/documentation/container/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/container/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-lib:classes
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-lib:test
```

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-lib:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-lib:testClasses
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-lib:test
```


