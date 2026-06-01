# Kora Examples

This directory contains standalone Kora example applications. The examples are broader module demonstrations, while [guides](../guides) contains companion applications for documentation guides.

Examples are split by language and target runtime:

- [Java examples](java)
- [Kotlin examples](kotlin)
- [GraalVM examples](graalvm)

Use the language-specific README files for the module catalog and links to Kora documentation.

## Common Commands

List all example projects:

```shell
./gradlew projects
```

Compile a single example:

```shell
./gradlew :examples:java:kora-java-cache-redis:classes
```

Run tests for a single example:

```shell
./gradlew :examples:java:kora-java-cache-redis:test
```
