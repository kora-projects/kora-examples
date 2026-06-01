# Kora Guides

This directory contains runnable companion applications for Kora documentation guides. There are also a more broader demonstrations of [examples](../examples) outside if guide scope.

The guide apps are split by language:

- [Java guide apps](java)
- [Kotlin guide apps](kotlin)

Each application README contains the matching documentation links, covered Kora modules, build commands, run commands when the module is executable, and test commands. Some guides use Docker/Testcontainers for local infrastructure; those README files call that out explicitly.

## Common Commands

List all guide projects:

```shell
./gradlew projects
```

Compile guide classes and generated sources through the individual module paths listed in [Java](java) and [Kotlin](kotlin) indexes.

```shell
./gradlew :guides:java:kora-java-guide-cache-app:classes
```

Compile test classes for a module before running the full tests:

```shell
./gradlew :guides:java:kora-java-guide-cache-app:testClasses
```

