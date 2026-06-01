# Kora Guide Database Cassandra App

This module is the runnable Java/Gradle companion application for the [Database Cassandra guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/database-cassandra.md). It builds the database guide variant on Cassandra-style persistence with entity mapping, repositories, generated Cassandra support, and integration testing.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/database-cassandra)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/database-cassandra)

## Kora Modules Covered
- Database Common: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-common/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-common/).
- Database Cassandra: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-cassandra/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-cassandra/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-database-cassandra-app:classes
./gradlew :guides:java:kora-java-guide-database-cassandra-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-database-cassandra-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-database-cassandra-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-database-cassandra-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-database-cassandra-app:run
```

## Local Infrastructure

Start companion infrastructure when running the application manually against local services:

```shell
docker compose -f guides/java/kora-java-guide-database-cassandra-app/docker-compose.yml up -d
```

Stop it when finished:

```shell
docker compose -f guides/java/kora-java-guide-database-cassandra-app/docker-compose.yml down
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-database-cassandra-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


