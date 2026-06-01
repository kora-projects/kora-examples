# Kora Guide Database JDBC Advanced App

This module is the runnable Java/Gradle companion application for the [Database JDBC Advanced guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/database-jdbc-advanced.md). It extends JDBC persistence with a related tasks table, query projections, SQL macros, custom mappers, and transactional batch operations.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/database-jdbc-advanced)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/database-jdbc-advanced)

## Kora Modules Covered
- Database Common: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-common/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-common/).
- Database JDBC: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-jdbc/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-jdbc/).
- Database Migration: [EN](https://kora-projects.github.io/kora-docs/en/documentation/database-migration/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/database-migration/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-database-jdbc-advanced-app:classes
./gradlew :guides:java:kora-java-guide-database-jdbc-advanced-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-advanced-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-advanced-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-advanced-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-advanced-app:run
```

## Local Infrastructure

Start companion infrastructure when running the application manually against local services:

```shell
docker compose -f guides/java/kora-java-guide-database-jdbc-advanced-app/docker-compose.yml up -d
```

Stop it when finished:

```shell
docker compose -f guides/java/kora-java-guide-database-jdbc-advanced-app/docker-compose.yml down
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-database-jdbc-advanced-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


