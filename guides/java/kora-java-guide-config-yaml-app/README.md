# Kora Guide Config YAML App

This module is the runnable Java/Gradle companion application for the [Config YAML guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/config-yaml.md). It demonstrates the same typed configuration model with YAML files, environment values, and generated config extractors.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/config-yaml)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/config-yaml)

## Kora Modules Covered
- Configuration: [EN](https://kora-projects.github.io/kora-docs/en/documentation/config/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/config/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-config-yaml-app:classes
./gradlew :guides:java:kora-java-guide-config-yaml-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-config-yaml-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-config-yaml-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-config-yaml-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-config-yaml-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-config-yaml-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


