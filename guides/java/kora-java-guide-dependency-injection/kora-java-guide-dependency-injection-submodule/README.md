# Kora Guide Dependency Injection Submodule

This module is part of the Java multi-module companion project for the [Dependency Injection guide](../../../agents-md/kora-docs/mkdocs/docs/en/guides/dependency-injection.md). It contains additional components contributed by a sibling Gradle module to demonstrate cross-module graph assembly.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/dependency-injection)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/dependency-injection)

## Kora Modules Covered
- Compile-time dependency injection container: [EN](https://kora-projects.github.io/kora-docs/en/documentation/container/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/container/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-submodule:classes
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-submodule:test
```

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-submodule:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-submodule:testClasses
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-submodule:test
```


