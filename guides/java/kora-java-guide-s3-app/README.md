# Kora Guide S3 App

This module is the runnable Java/Gradle companion application for the [S3 guide](../../agents-md/kora-docs/mkdocs/docs/en/guides/s3.md). It demonstrates Kora S3 client integration, object upload/download operations, MinIO-compatible local testing, and typed service code around object storage.

## Documentation
- [Guide in English](https://kora-projects.github.io/kora-docs/en/guides/s3)
- [Руководство на Русском](https://kora-projects.github.io/kora-docs/ru/guides/s3)

## Kora Modules Covered
- S3 Client: [EN](https://kora-projects.github.io/kora-docs/en/documentation/s3-client/) / [RU](https://kora-projects.github.io/kora-docs/ru/documentation/s3-client/).

## Run Checks
```bash
./gradlew :guides:java:kora-java-guide-s3-app:classes
./gradlew :guides:java:kora-java-guide-s3-app:test
```

Use this module as the executable reference while reading the guide; the guide explains the concepts, and this app keeps the complete buildable source in one place.

## Build

Compile production classes:

```shell
./gradlew :guides:java:kora-java-guide-s3-app:classes
```

Compile test classes without running tests:

```shell
./gradlew :guides:java:kora-java-guide-s3-app:testClasses
```

Build the runnable distribution archive:

```shell
./gradlew :guides:java:kora-java-guide-s3-app:distTar
```

## Run

Run the application locally:

```shell
./gradlew :guides:java:kora-java-guide-s3-app:run
```

## Local Infrastructure

Start companion infrastructure when running the application manually against local services:

```shell
docker compose -f guides/java/kora-java-guide-s3-app/docker-compose.yml up -d
```

Stop it when finished:

```shell
docker compose -f guides/java/kora-java-guide-s3-app/docker-compose.yml down
```

## Test

Tests use Testcontainers or companion containerized services, so Docker or a compatible container runtime must be available.

Run tests locally:

```shell
./gradlew :guides:java:kora-java-guide-s3-app:test
```

## Generated Sources and Artifacts

- `distTar` produces `build/distributions/application.tar`.


