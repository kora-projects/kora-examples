# Kora Kotlin Guide Dependency Injection

This module is the library Kotlin/Gradle companion project for the [Dependency Injection guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/dependency-injection.md). Compile-time dependency injection across several Gradle modules and reusable Kora components.

## Documentation

- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/dependency-injection)
- [Guide in Russian](https://kora-projects.github.io/kora-docs/ru/guides/dependency-injection)
- [Kora documentation: Dependency Injection](https://kora-projects.github.io/kora-docs/en/documentation/container/)

## Kora Modules Covered

- Container, KoraApp, Module

## Build

Compile production classes:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-submodule:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-submodule:testClasses
```

## Test

Run the module tests with JUnit 5 and Kora test support.

```shell
./gradlew :guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-submodule:test
```

## Notes

- Use this module as the executable reference while reading the guide; the guide explains the concepts, and this project keeps the complete buildable source in one place.


