# Kora Guide Testing JUnit App

This module is the runnable Java/Gradle companion application for the [Testing JUnit guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/testing-junit.md). It demonstrates Kora JUnit 5 component tests, graph customization, component injection into tests, and controlled test-time dependencies.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/testing-junit)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/testing-junit)

## Kora Modules Covered
- JUnit 5 testing: [EN](https://kora-projects.github.io/kora-docs/en/documentation/junit5/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/junit5/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-testing-junit-app:classes
./gradlew :guides:java:kora-java-guide-testing-junit-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-testing-junit-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-testing-junit-app:testClasses
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-testing-junit-app:test
```


