# Kora Guide Dependency Injection Workspace

This workspace contains the multi-module Java companion project for the [Dependency Injection guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/dependency-injection.md). It shows how a Kora application graph can be assembled from several Gradle modules while still relying on compile-time dependency discovery and generated wiring.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/dependency-injection)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/dependency-injection)

## Modules
- `guide-dependency-injection-app` is the executable Kora application module.
- `guide-dependency-injection-common` contains shared contracts used across the graph.
- `guide-dependency-injection-lib` contributes reusable components from a library module.
- `guide-dependency-injection-submodule` contributes additional application components from a sibling module.

## Kora Modules Covered
- Compile-time dependency injection container: [EN](https://kora-projects.github.io/kora-docs/en/documentation/container/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/container/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:classes
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:test
```

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:testClasses
```

## Test

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-dependency-injection:kora-java-guide-dependency-injection-app:test
```


