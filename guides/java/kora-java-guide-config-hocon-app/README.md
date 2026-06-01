# Kora Guide Config HOCON App

This module is the runnable Java/Gradle companion application for the [Config HOCON guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/config-hocon.md). It demonstrates typed configuration binding with HOCON, environment overrides, nested configuration objects, and generated config extractors.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/config-hocon)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/config-hocon)

## Kora Modules Covered
- Configuration: [EN](https://kora-projects.github.io/kora-docs/en/documentation/config/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/config/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-config-hocon-app:classes
./gradlew :guides:java:kora-java-guide-config-hocon-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-config-hocon-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-config-hocon-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-config-hocon-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-config-hocon-app:run
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-config-hocon-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


