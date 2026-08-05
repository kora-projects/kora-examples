# Kora 1.x -> 2.0 migration guide

This file is the living migration record for this repository. It is updated after every migrated Gradle module and verified against `agents-md/kora-2`.

## Scope and sources

- 1.x baseline: `agents-md/kora-docs` and Git revision before `0ae3b7f`.
- 2.0 source of truth: `agents-md/kora-2`.
- Verified reference application: `examples/java/kora-java-crud`.
- One service equals one Gradle leaf module.
- Removed modules are not deleted automatically. They are excluded from the aggregate build and listed below for manual deletion.

## Migration order

1. Run the OpenRewrite recipe for Java AST-safe package/type/dependency changes.
2. Run `python migration/scripts/migrate_kora_2.py --apply` for Gradle/Kotlin/OpenAPI/resource changes and package-directory moves that OpenRewrite cannot safely cover.
3. Compile one module with `./gradlew <module>:classes --console plain`.
4. Apply module-specific semantic changes from this guide.
5. Run that module's tests before moving to the next module.

Both automation layers are idempotent. Always commit or back up the target repository first and inspect the dry-run output.

## Repository-wide mandatory changes

### Runtime and build toolchain

- Java toolchain: `21` -> `25`. Kora 2.0 artifacts are compiled for JVM 25 and Gradle rejects them on a Java 21 compile classpath.
- Kotlin Gradle plugin: `1.9.25` -> `2.4.10`.
- KSP plugin: `1.9.25-1.0.20` -> `2.3.11`.
- OpenAPI Generator Gradle plugin: use the version aligned with Kora 2 source (`7.24.0`).
- KSP 2 no longer exposes the old `com.google.devtools.ksp.gradle.KspTask` type used by these build scripts. Replace `tasks.withType<KspTask>()` wiring with name-based task wiring such as `tasks.matching { it.name.startsWith("ksp") }`, or wire the exact generated KSP task by name.
- Maven group for Kora artifacts: `ru.tinkoff.kora` -> `io.koraframework`.
- BOM stays named `kora-parent`: `io.koraframework:kora-parent:2.0.0-SNAPSHOT` for the local snapshot used by this repository.

### Namespace

Framework packages moved from `ru.tinkoff.kora.*` to `io.koraframework.*`. This repository also renames its example/guide packages from `ru.tinkoff.kora.*` to `io.koraframework.*`, matching the already migrated CRUD example. For application repositories whose own package does not start with `ru.tinkoff.kora`, only framework imports change.

Core DI annotations moved one level deeper:

- `ru.tinkoff.kora.common.KoraApp` -> `io.koraframework.common.annotation.KoraApp`
- `Component`, `DefaultComponent`, `KoraSubmodule`, `Mapping`, `Module`, and `Tag` follow the same `io.koraframework.common.annotation.*` pattern.
- `Context` was removed from synchronous HTTP APIs; do not mechanically replace it. Remove the argument and migrate the affected callback/interceptor signature.

### Dependency/module renames

- `json-module` -> `json-common`; `JsonModule` is now `io.koraframework.json.common.JsonModule`.
- `cache-redis` -> `cache-redis-lettuce`.
- Applications using Redis cache must include `LettuceRedisCacheModule` instead of the transport-neutral `RedisCacheModule`; the latter no longer provides `RedisCacheClient` by itself.
- `http-client-async` has no Kora 2 module and must be migrated to a supported client implementation such as `http-client-jdk` or `http-client-ok`.
- `jakarta.annotation.Nullable` -> JSpecify `org.jspecify.annotations.Nullable`; JSpecify is exposed transitively by Kora core. Redundant `@Nonnull` annotations can be removed; if null-marking is required, prefer package/module-level JSpecify `@NullMarked` and place type-use annotations correctly on qualified nested types.
- In Kotlin, remove Java/JSpecify `@Nullable` use-site annotations and express nullability with `T?`; `@field:Nullable` is not a valid JSpecify target under Kotlin 2.4.
- `@ConfigValueExtractor` -> `@ConfigMapper`.

### HTTP server

- `UndertowHttpServerModule` -> `UndertowPublicHttpServerModule`.
- HTTP server execution is synchronous in 2.0. Remove `CompletionStage`, Reactor wrappers, and explicit Kora `Context` from controller/interceptor contracts where the new API returns `HttpServerResponse` directly.
- `HttpServerInterceptor.intercept(...)` now accepts `(HttpServerRequest, InterceptChain)` and calls `chain.process(request)`.
- HTTP types were split into subpackages: requests and request mappers/handlers moved to `http.server.common.request`, responses and response mappers/exceptions to `http.server.common.response`, interceptors to `http.server.common.interceptor`.
- JSON writers use `toByteArray(value)`; old `toByteArrayUnchecked(value)` is gone.
- Custom interceptors and response/request mappers referenced by `@InterceptWith` / `@Mapping` must be DI components (`@Component`) unless a module method provides them.

### OpenAPI generation

Only these modes exist in Kora 2: `java-client`, `java-server`, `kotlin-client`, `kotlin-server`.

- `java-reactive-client` -> `java-client`
- `java-reactive-server` -> `java-server`
- `kotlin-suspend-client` -> `kotlin-client`
- `kotlin-suspend-server` / `kotlin-reactive-server` -> `kotlin-server`

Generated code must then be adapted to synchronous Kora 2 APIs; changing the mode alone is necessary but not always sufficient.

### Resilience

String-based annotations were replaced by typed specs/components:

- `@CircuitBreaker("pet")` -> `@CircuitBreakable(PetCircuitBreaker.class)` plus an interface extending `CircuitBreaker` and annotated with `@CircuitBreakerSpec("resilient.circuitbreaker.pet")`.
- `@Retry("pet")` -> `@Retryable(PetRetry.class)` plus `Retry`/`@RetrySpec` interface.
- `@Timeout("pet")` -> `@Timeout(PetTimeouter.class)` plus `Timeouter`/`@TimeoutSpec` interface.
- Fixed-window circuit breaker config requires `type = FIXED_WINDOW`.
- `@Fallback` no longer has a config-name `value`; keep only `method = "fallback(...)"`.
- Circuit breaker `slidingWindowSize` -> `countBased.windowSize`.

### Cache

- Cache key selector attribute: `parameters` -> `args`.
- Whole-cache invalidation moved from `@CacheInvalidate(..., invalidateAll = true)` to dedicated `@CacheInvalidateAll(CacheType.class)` / Kotlin `CacheType::class`.
- Custom cache key mapper classes referenced by `@Mapping` must be DI components (for example, annotate nested `UserContextMapping` with `@Component`) because generated AOP proxies now inject them.

### Scheduling

- Quartz `@ScheduleWithTrigger` now accepts the tag class directly: `@ScheduleWithTrigger(@Tag(MyJob.class))` -> `@ScheduleWithTrigger(MyJob.class)`; Kotlin `Tag(MyJob::class)` -> `MyJob::class`.

## Removed or currently non-migratable modules

These Kora 1.x integrations do not exist in `agents-md/kora-2` and must not remain in the aggregate 2.0 build:

- Java/Kotlin `database-r2dbc` examples.
- Java/Kotlin `database-vertx` examples.
- GraalVM CRUD R2DBC and Vert.x examples.

The source directories are retained for manual deletion. Final status and any additional removed integrations will be appended after full inventory.

## Module status

- `examples:java:kora-java-crud` — migrated by user; compiles and tests pass; reference module.
- All other modules — in progress.

## Automation limits

OpenRewrite handles Java syntax, type/package changes, and Gradle dependency coordinates. It cannot reliably migrate arbitrary Groovy/Kotlin DSL maps, generated-code mode strings, source directory moves, HOCON semantic changes, or design new typed resilience specs. `migration/scripts/migrate_kora_2.py` handles deterministic text/filesystem changes. Semantic API redesign remains module-specific and is verified by compilation/tests.
