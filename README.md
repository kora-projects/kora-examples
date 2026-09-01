<p align="center">
  <a href="https://kora-projects.github.io/kora-docs">
    <img src="https://kora-projects.github.io/kora-docs/v2/en/assets/img/kora-long.png" alt="Kora Framework" width="420">
  </a>
</p>

<h1 align="center">Kora Playground</h1>

<p align="center">
  <b>The hands-on playground for the <a href="https://kora-projects.github.io/kora-docs">Kora framework</a>.</b><br>
  Clone it, run any module, poke at real endpoints, break things, and see how Kora works — in Java and Kotlin.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.koraframework/common"><img src="https://img.shields.io/maven-central/v/io.koraframework/common.svg?label=maven%20central" alt="Maven Central"></a>
  <a href="https://github.com/kora-projects/kora-examples/actions?query=workflow%3A%22Build+Master%22"><img src="https://github.com/kora-projects/kora-examples/workflows/Build%20Master/badge.svg" alt="Build"></a>
  <a href="https://github.com/kora-projects/kora-examples/blob/master/LICENSE"><img src="https://img.shields.io/github/license/kora-projects/kora-examples.svg" alt="License"></a>
</p>

<p align="center">
  <a href="https://github.com/kora-projects/kora">Framework</a> ·
  <a href="https://kora-projects.github.io/kora-docs">Documentation</a> ·
  <a href="https://kora-projects.github.io/kora-docs/guides">Getting Started</a> ·
  <a href="https://github.com/kora-projects/kora-skills">Kora Skills</a>
</p>

> Русская версия: [README.ru.md](README.ru.md)

---

This is the sandbox for the [Kora framework](https://kora-projects.github.io/kora-docs). Every module here is a small, self-contained service that runs one part of Kora end to end — real code, real config, real tests. There is nothing to wire up: check out the repo, start a module, send it a request, then open the source and change something to see what happens.

Everything comes in **both Java and Kotlin**, so you can play in whichever language you ship.

**Even better with an AI agent.** Install our [kora-skills](https://github.com/kora-projects/kora-skills) skill and let your AI agent run these examples, explain what Kora generated, and modify them with you — straight from the official guides and code.

## Play with it in a minute

```bash
git clone https://github.com/kora-projects/kora-examples.git
cd kora-examples

# start any module — e.g. the HTTP server
./gradlew :kora-java-http-server:run
```

Then hit it, change a handler, and re-run:

```bash
curl http://localhost:8080/hello
```

Each example is an **independent Gradle module**, so you can also just build or test one in isolation:

```bash
./gradlew :kora-java-http-server:build     # build one module
./gradlew :kora-java-http-server:test      # run its tests
./gradlew build                            # build everything
```

Modules that need infrastructure (PostgreSQL, Kafka, Cassandra, S3, …) ship a `docker-compose.yml` and a `Dockerfile` next to their code — bring the dependencies up with `docker compose up` inside the module first. **Every module has its own `README.md`** with the exact commands, ports, and example requests.

> Requires **JDK 17+**; the Gradle wrapper (`./gradlew`) is included, so nothing else to install.

## What to try first

- **[Hello World](examples/java/kora-java-helloworld)** — the smallest Kora service.
- **[Getting Started app](guides/java/kora-java-guide-getting-started-app)** — the guided first service.
- **[Dependency Injection intro](guides/java/kora-java-guide-dependency-injection-introduction-app)** — how the compile-time graph is wired.
- **[Full CRUD service](examples/java/kora-java-crud)** — HTTP + JDBC + tests together, closest to a real app.
- Run `./gradlew :<module>:test` on any module and open `build/generated/…` to read the code Kora generated for it.

## Two folders

- **[`examples/`](examples)** — standalone demonstration services, one per Kora module. Reach for these to play with a single feature in isolation.
- **[`guides/`](guides)** — companion apps for the step-by-step [documentation guides](https://kora-projects.github.io/kora-docs/guides): read the guide, run its app, follow along.

## What you can play with

Each row links the same runnable service in both languages.

### Getting started & core

| Topic | Java | Kotlin |
|---|---|---|
| Hello World | [Java](examples/java/kora-java-helloworld) | [Kotlin](examples/kotlin/kora-kotlin-helloworld) |
| Full CRUD service | [Java](examples/java/kora-java-crud) | [Kotlin](examples/kotlin/kora-kotlin-crud) |
| CRUD with `@KoraSubmodule` | [Java](examples/java/kora-java-crud-submodule) | [Kotlin](examples/kotlin/kora-kotlin-crud-submodule) |
| Config — HOCON | [Java](examples/java/kora-java-config-hocon) | [Kotlin](examples/kotlin/kora-kotlin-config-hocon) |
| Config — YAML | [Java](examples/java/kora-java-config-yaml) | [Kotlin](examples/kotlin/kora-kotlin-config-yaml) |

### HTTP & API

| Topic | Java | Kotlin |
|---|---|---|
| HTTP server | [Java](examples/java/kora-java-http-server) | [Kotlin](examples/kotlin/kora-kotlin-http-server) |
| HTTP client | [Java](examples/java/kora-java-http-client) | [Kotlin](examples/kotlin/kora-kotlin-http-client) |
| OpenAPI → HTTP server | [Java](examples/java/kora-java-openapi-generator-http-server) | [Kotlin](examples/kotlin/kora-kotlin-openapi-generator-http-server) |
| OpenAPI → HTTP client | [Java](examples/java/kora-java-openapi-generator-http-client) | [Kotlin](examples/kotlin/kora-kotlin-openapi-generator-http-client) |
| SOAP client | [Java](examples/java/kora-java-soap-client) | [Kotlin](examples/kotlin/kora-kotlin-soap-client) |

### Databases

| Topic | Java | Kotlin |
|---|---|---|
| JDBC | [Java](examples/java/kora-java-database-jdbc) | [Kotlin](examples/kotlin/kora-kotlin-database-jdbc) |
| R2DBC | [Java](examples/java/kora-java-database-r2dbc) | [Kotlin](examples/kotlin/kora-kotlin-database-r2dbc) |
| Vert.x | [Java](examples/java/kora-java-database-vertx) | [Kotlin](examples/kotlin/kora-kotlin-database-vertx) |
| Cassandra | [Java](examples/java/kora-java-database-cassandra) | [Kotlin](examples/kotlin/kora-kotlin-database-cassandra) |

### Messaging & RPC

| Topic | Java | Kotlin |
|---|---|---|
| Kafka | [Java](examples/java/kora-java-kafka) | [Kotlin](examples/kotlin/kora-kotlin-kafka) |
| gRPC server | [Java](examples/java/kora-java-grpc-server) | [Kotlin](examples/kotlin/kora-kotlin-grpc-server) |
| gRPC client | [Java](examples/java/kora-java-grpc-client) | [Kotlin](examples/kotlin/kora-kotlin-grpc-client) |
| S3 — AWS SDK | [Java](examples/java/kora-java-s3-client-aws) | [Kotlin](examples/kotlin/kora-kotlin-s3-client-aws) |
| S3 — MinIO | [Java](examples/java/kora-java-s3-client-minio) | [Kotlin](examples/kotlin/kora-kotlin-s3-client-minio) |

### Resilience & aspects

| Topic | Java | Kotlin |
|---|---|---|
| Resilience (`@Retry`, `@Timeout`, `@CircuitBreaker`, `@Fallback`) | [Java](examples/java/kora-java-resilient) | [Kotlin](examples/kotlin/kora-kotlin-resilient) |
| Cache — Caffeine | [Java](examples/java/kora-java-cache-caffeine) | [Kotlin](examples/kotlin/kora-kotlin-cache-caffeine) |
| Cache — Redis | [Java](examples/java/kora-java-cache-redis) | [Kotlin](examples/kotlin/kora-kotlin-cache-redis) |
| Validation | [Java](examples/java/kora-java-validation) | [Kotlin](examples/kotlin/kora-kotlin-validation) |
| Scheduling — JDK | [Java](examples/java/kora-java-scheduling-jdk) | [Kotlin](examples/kotlin/kora-kotlin-scheduling-jdk) |
| Scheduling — Quartz | [Java](examples/java/kora-java-scheduling-quartz) | [Kotlin](examples/kotlin/kora-kotlin-scheduling-quartz) |

### Observability & workflow

| Topic | Java | Kotlin |
|---|---|---|
| Telemetry (metrics, tracing, logs) | [Java](examples/java/kora-java-telemetry) | [Kotlin](examples/kotlin/kora-kotlin-telemetry) |
| Camunda 7 engine | [Java](examples/java/kora-java-camunda-engine) | [Kotlin](examples/kotlin/kora-kotlin-camunda-engine) |
| Camunda 8 Zeebe worker | [Java](examples/java/kora-java-camunda-zeebe-worker) | [Kotlin](examples/kotlin/kora-kotlin-camunda-zeebe-worker) |

### GraalVM Native Image

Ahead-of-time compiled native builds — see [`examples/graalvm`](examples/graalvm):

[CRUD · JDBC](examples/graalvm/kora-java-graalvm-crud-jdbc) · [CRUD · R2DBC](examples/graalvm/kora-java-graalvm-crud-r2dbc) · [CRUD · Vert.x](examples/graalvm/kora-java-graalvm-crud-vertx) · [CRUD · Cassandra](examples/graalvm/kora-java-graalvm-crud-cassandra) · [Kafka](examples/graalvm/kora-java-graalvm-kafka)

## Guides

The [`guides/`](guides) tree holds one companion app per documentation guide, in [Java](guides/java) and [Kotlin](guides/kotlin) — from getting started and dependency injection to databases, HTTP, Kafka, gRPC, resilience, validation, observability, and the three testing styles (JUnit, integration, black-box). Read a guide in the [docs](https://kora-projects.github.io/kora-docs/guides) and run its app side by side.

## Documentation & community

- Documentation — [kora-docs](https://kora-projects.github.io/kora-docs)
- Guides — [Getting Started](https://kora-projects.github.io/kora-docs/guides)
- Framework source — [kora-projects/kora](https://github.com/kora-projects/kora)
- AI skill — [kora-skills](https://github.com/kora-projects/kora-skills)

## License

Licensed under the [Apache License 2.0](https://github.com/kora-projects/kora-examples/blob/master/LICENSE).
