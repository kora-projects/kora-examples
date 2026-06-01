# Kora Guide Database JDBC App

This module is the runnable Java/Gradle companion application for the [Database JDBC guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/database-jdbc.md). It builds a PostgreSQL-backed HTTP API with Flyway migrations, JDBC repositories, entity mapping, transactions, and Testcontainers-based verification.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/database-jdbc)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/database-jdbc)

## Kora Modules Covered
- Database Common: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-common/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-common/).
- Database JDBC: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-jdbc/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-jdbc/).
- Database Migration: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-migration/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-migration/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-database-jdbc-app:classes
./gradlew :guides:java:kora-java-guide-database-jdbc-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-app:run
```

## Local Infrastructure

Start companion infrastructure when running the application manually against local services:

```shell
docker compose -f guides/java/kora-java-guide-database-jdbc-app/docker-compose.yml up -d
```

Stop it when finished:

```shell
docker compose -f guides/java/kora-java-guide-database-jdbc-app/docker-compose.yml down
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


