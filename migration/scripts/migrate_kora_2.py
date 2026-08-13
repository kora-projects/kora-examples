#!/usr/bin/env python3
"""Idempotent Kora 1.x -> 2.0 mechanical migration.

Dry-run by default. Pass --apply to write. Semantic API changes are deliberately
excluded and must be handled using KORA_2_MIGRATION_GUIDE.md.
"""

from __future__ import annotations

import argparse
import os
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SCAN_ROOTS = (ROOT / "examples", ROOT / "guides")
TEXT_SUFFIXES = {
    ".java", ".kt", ".kts", ".gradle", ".properties", ".conf", ".yaml",
    ".yml", ".json", ".xml", ".proto",
}
SKIP_PARTS = {"build", ".gradle", ".git"}
LEGACY_APT_TOKEN = "ka" + "pt"

REPLACEMENTS = (
    ("1.9.25-1.0.20", "2.3.11"),
    ("1.9.25", "2.4.10"),
    ('id("org.openapi.generator") version ("7.23.0")', 'id("org.openapi.generator") version ("7.24.0")'),
    ('id("org.openapi.generator") version "7.23.0"', 'id("org.openapi.generator") version "7.24.0"'),
    ("import com.google.devtools.ksp.gradle.KspTask\n", ""),
    ('tasks.withType<KspTask>().configureEach {', 'tasks.matching { it.name.startsWith("ksp") }.configureEach {'),
    ('id("org.jetbrains.kotlin.jvm") version "1.9.25"', 'id("org.jetbrains.kotlin.jvm") version "2.4.10"'),
    ('id("com.google.devtools.ksp") version "1.9.25-1.0.20"', 'id("com.google.devtools.ksp") version "2.3.11"'),
    ("ru.tinkoff.kora", "io.koraframework"),
    ("io.koraframework:kora-parent", "io.koraframework:kora-bom"),
    ("io.koraframework.common.KoraApp", "io.koraframework.common.annotation.KoraApp"),
    ("io.koraframework.common.Component", "io.koraframework.common.annotation.Component"),
    ("io.koraframework.common.DefaultComponent", "io.koraframework.common.annotation.DefaultComponent"),
    ("io.koraframework.common.KoraSubmodule", "io.koraframework.common.annotation.KoraSubmodule"),
    ("io.koraframework.common.Mapping", "io.koraframework.common.annotation.Mapping"),
    ("io.koraframework.common.Module", "io.koraframework.common.annotation.Module"),
    ("io.koraframework.common.Tag", "io.koraframework.common.annotation.Tag"),
    ("io.koraframework.http.server.undertow.UndertowHttpServerModule", "io.koraframework.http.server.undertow.UndertowPublicHttpServerModule"),
    ("UndertowHttpServerModule", "UndertowPublicHttpServerModule"),
    ("io.koraframework.http.server.common.HttpServerInterceptor", "io.koraframework.http.server.common.interceptor.HttpServerInterceptor"),
    ("io.koraframework.http.server.common.HttpServerRequest", "io.koraframework.http.server.common.request.HttpServerRequest"),
    ("io.koraframework.http.server.common.HttpServerResponseException", "io.koraframework.http.server.common.response.HttpServerResponseException"),
    ("io.koraframework.http.server.common.HttpServerResponse", "io.koraframework.http.server.common.response.HttpServerResponse"),
    ("io.koraframework.http.server.common.handler.HttpServerRequestHandlerImpl", "io.koraframework.http.server.common.request.HttpServerRequestHandlerImpl"),
    ("io.koraframework.http.server.common.handler.HttpServerRequestHandler", "io.koraframework.http.server.common.request.HttpServerRequestHandler"),
    ("io.koraframework.http.server.common.handler.HttpServerRequestMapper", "io.koraframework.http.server.common.request.HttpServerRequestMapper"),
    ("io.koraframework.http.server.common.handler.HttpServerResponseMapper", "io.koraframework.http.server.common.response.HttpServerResponseMapper"),
    ("io.koraframework.http.client.common.HttpClientDecoderException", "io.koraframework.http.client.common.exception.HttpClientDecoderException"),
    ("io.koraframework.http.client.common.HttpClientResponseException", "io.koraframework.http.client.common.exception.HttpClientResponseException"),
    ("io.koraframework.database.jdbc.EntityJdbc", "io.koraframework.database.jdbc.annotation.EntityJdbc"),
    ("io.koraframework.common.util.ByteBufferPublisherInputStream", "io.koraframework.common.util.ByteBufferInputStream"),
    ("io.koraframework.json.module.JsonModule", "io.koraframework.json.common.JsonModule"),
    ("io.koraframework:json-module", "io.koraframework:json-common"),
    ('io.koraframework:cache-redis"', 'io.koraframework:cache-redis-lettuce"'),
    ('io.koraframework:cache-redis")', 'io.koraframework:cache-redis-lettuce")'),
    ("io.koraframework:cache-redis-lettuce-lettuce", "io.koraframework:cache-redis-lettuce"),
    ("io.koraframework.cache.redis.RedisCacheModule", "io.koraframework.cache.redis.lettuce.LettuceRedisCacheModule"),
    ("        RedisCacheModule {", "        LettuceRedisCacheModule {"),
    ("        RedisCacheModule,", "        LettuceRedisCacheModule,"),
    ("io.koraframework.experimental:s3-client-aws", "io.koraframework:s3-client-aws"),
    ('    implementation "io.koraframework:http-client-async"\n', ""),
    ('    implementation("io.koraframework:http-client-async")\n', ""),
    ("jakarta.annotation.Nullable", "org.jspecify.annotations.Nullable"),
    ("import jakarta.annotation.Nonnull;\n", ""),
    ("import org.jspecify.annotations.NonNull;\n", ""),
    ("@Nonnull\n", ""),
    ("@NonNull\n", ""),
    ("ConfigValueExtractor", "ConfigMapper"),
    ("parameters =", "args ="),
    ("@CacheInvalidate(value = SimpleCache.class, invalidateAll = true)", "@CacheInvalidateAll(SimpleCache.class)"),
    ("@CacheInvalidate(value = CompositeCache.class, invalidateAll = true)", "@CacheInvalidateAll(CompositeCache.class)"),
    ("@CacheInvalidate(value = SimpleCache::class, invalidateAll = true)", "@CacheInvalidateAll(SimpleCache::class)"),
    ("@CacheInvalidate(value = CompositeCache::class, invalidateAll = true)", "@CacheInvalidateAll(CompositeCache::class)"),
    ('mode                  : "java-reactive-server"', 'mode                  : "java-server"'),
    ('mode                : "java-reactive-client"', 'mode                : "java-client"'),
    ('"mode" to "kotlin-suspend-client"', '"mode" to "kotlin-client"'),
    ('"mode" to "kotlin-suspend-server"', '"mode" to "kotlin-server"'),
    ('"mode" to "kotlin-reactive-server"', '"mode" to "kotlin-server"'),
    ("JavaLanguageVersion.of(21)", "JavaLanguageVersion.of(25)"),
)

ROOT_REPLACEMENTS = (
    ('id "org.jetbrains.kotlin.jvm" version "1.9.25"', 'id "org.jetbrains.kotlin.jvm" version "2.4.10"'),
    ('id "com.google.devtools.ksp" version "1.9.25-1.0.20"', 'id "com.google.devtools.ksp" version "2.3.11"'),
)


def text_files():
    for scan_root in SCAN_ROOTS:
        for path in scan_root.rglob("*"):
            if path.is_file() and path.suffix in TEXT_SUFFIXES and not SKIP_PARTS.intersection(path.parts):
                yield path


def replace_text(path: Path, replacements, apply: bool) -> bool:
    original = path.read_text(encoding="utf-8")
    updated = original
    for old, new in replacements:
        updated = updated.replace(old, new)
    updated = update_junit_version(updated)
    updated = update_mockk_version(updated)
    if path.name in {"build.gradle", "build.gradle.kts"}:
        updated = remove_empty_environment_block(updated)
        updated = "".join(line for line in updated.splitlines(keepends=True) if LEGACY_APT_TOKEN not in line.lower())
    if "@CacheInvalidateAll" in updated and "cache.annotation.CacheInvalidateAll" not in updated:
        updated = updated.replace(
            "import io.koraframework.cache.annotation.CacheInvalidate;",
            "import io.koraframework.cache.annotation.CacheInvalidate;\nimport io.koraframework.cache.annotation.CacheInvalidateAll;",
        )
        updated = updated.replace(
            "import io.koraframework.cache.annotation.CacheInvalidate\n",
            "import io.koraframework.cache.annotation.CacheInvalidate\nimport io.koraframework.cache.annotation.CacheInvalidateAll\n",
        )
    if path.suffix == ".kt":
        if "@HttpClient" in updated:
            updated = re.sub(r"(?m)^([ \t]*)suspend fun ", r"\1fun ", updated)
        if "@Repository" in updated:
            updated = re.sub(r"(?m)^([ \t]*)suspend fun ", r"\1fun ", updated)
        if "@HttpController" in updated or "@HttpRoute" in updated:
            updated = re.sub(r"(?m)^([ \t]*)suspend fun ", r"\1fun ", updated)
        updated = updated.replace("import org.jspecify.annotations.Nullable\n", "")
        updated = updated.replace("import org.jspecify.annotations.NonNull\n", "")
        updated = updated.replace("@field:Nullable ", "")
        updated = updated.replace("@Nullable ", "")
        updated = updated.replace("    @Nullable\n", "")
        if "io.koraframework.cache.redis.lettuce.LettuceRedisCacheModule" in updated:
            updated = updated.replace("    RedisCacheModule,", "    LettuceRedisCacheModule,")
            updated = updated.replace(", RedisCacheModule", ", LettuceRedisCacheModule")
        if "@Mapping(UserContextMapping::class)" in updated and "@Component\n    class UserContextMapping" not in updated:
            updated = updated.replace(
                "    class UserContextMapping",
                "    @Component\n    class UserContextMapping",
            )
    if path.name.endswith(".gradle.kts"):
        updated = normalize_kotlin_dependencies(path, updated)
    if "@Mapping(UserContextMapping" in updated and "@Component\n    public static final class UserContextMapping" not in updated:
        updated = updated.replace(
            "    public static final class UserContextMapping",
            "    @Component\n    public static final class UserContextMapping",
        )
    if updated == original:
        return False
    print(f"CHANGE {path.relative_to(ROOT)}")
    if apply:
        path.write_text(updated, encoding="utf-8", newline="")
    return True


def update_junit_version(text: str) -> str:
    """Update any literal JUnit version to the migration target, preserving Gradle syntax."""
    text = re.sub(
        r"(?m)^(\s*junitVersion\s*=\s*)[0-9][0-9A-Za-z.+_-]*(\s*(?:#.*)?)$",
        r"\g<1>6.1.3\g<2>",
        text,
    )
    return re.sub(
        r"(org\.junit:junit-bom:)[0-9][0-9A-Za-z.+_-]*",
        r"\g<1>6.1.3",
        text,
    )


def update_mockk_version(text: str) -> str:
    """Update explicitly pinned MockK to a Java 25-compatible release."""
    return re.sub(
        r"(io\.mockk:mockk:)[0-9][0-9A-Za-z.+_-]*",
        r"\g<1>1.14.9",
        text,
    )


def remove_empty_environment_block(text: str) -> str:
    """Remove invalid Gradle test environment entries with an empty variable name."""
    return re.sub(
        r'(?ms)\r?\n[ \t]*environment\(\[\s*\r?\n[ \t]*"": "",?\s*\r?\n[ \t]*\]\)\s*\r?\n',
        "\n",
        text,
    )


def normalize_kotlin_dependencies(path: Path, text: str) -> str:
    """Use direct versioned KSP and implementation-platform dependencies in Kotlin builds."""
    text = re.sub(
        r"(?ms)^[ \t]*val koraBom: Configuration by configurations\.creating\r?\n"
        r"[ \t]*configurations \{\r?\n.*?extendsFrom\(koraBom\).*?^[ \t]*\}\r?\n(?:\r?\n)?",
        "",
        text,
    )
    text = text.replace(
        'listOf("ksp", "implementation", "kspTest").forEach { name ->\n'
        '            named(name) { extendsFrom(koraBom) }\n'
        '        }',
        "",
    )
    dependency_lines = []
    for line in text.splitlines(keepends=True):
        if line.lstrip().startswith('koraBom(platform("io.koraframework:kora-bom:'):
            indent = line[:len(line) - len(line.lstrip())]
            newline = "\r\n" if line.endswith("\r\n") else "\n"
            if path == ROOT / "examples/kotlin/kora-kotlin-crud-submodule/build.gradle.kts":
                line = f'{indent}add("implementation", platform("io.koraframework:kora-bom:${{property("koraVersion")}}")){newline}'
            else:
                line = f'{indent}implementation(platform("io.koraframework:kora-bom:${{property("koraVersion")}}")){newline}'
        dependency_lines.append(line)
    text = "".join(dependency_lines)
    text = text.replace(
        'ksp("io.koraframework:symbol-processors")',
        'ksp("io.koraframework:symbol-processors:${property("koraVersion")}")',
    )
    text = text.replace(
        'add("ksp", "io.koraframework:symbol-processors")',
        'add("ksp", "io.koraframework:symbol-processors:${property("koraVersion")}")',
    )
    lines = []
    for line in text.splitlines(keepends=True):
        lowered = line.lower()
        if LEGACY_APT_TOKEN in lowered:
            continue
        lines.append(line)
    text = "".join(lines)
    text = text.replace(
        'kspTest("io.koraframework:symbol-processors")',
        'kspTest("io.koraframework:symbol-processors:${property("koraVersion")}")',
    )
    return text


def package_moves(apply: bool) -> int:
    moves = []
    for scan_root in SCAN_ROOTS:
        for old in scan_root.rglob("ru/tinkoff/kora"):
            if old.is_dir() and not SKIP_PARTS.intersection(old.parts):
                relative_tail = old.relative_to(scan_root)
                prefix = relative_tail.parts[:-3]
                new = scan_root.joinpath(*prefix, "io", "koraframework")
                moves.append((old, new))
    for old, new in sorted(moves, key=lambda pair: len(pair[0].parts), reverse=True):
        print(f"MOVE   {old.relative_to(ROOT)} -> {new.relative_to(ROOT)}")
        if not apply:
            continue
        new.parent.mkdir(parents=True, exist_ok=True)
        if new.exists():
            for source in old.rglob("*"):
                if source.is_file():
                    target = new / source.relative_to(old)
                    if target.exists():
                        raise RuntimeError(f"Move collision: {target}")
                    target.parent.mkdir(parents=True, exist_ok=True)
                    source.replace(target)
            for directory, _, _ in os.walk(old, topdown=False):
                Path(directory).rmdir()
        else:
            old.replace(new)
    return len(moves)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true", help="write changes; default is dry-run")
    args = parser.parse_args()

    changed = sum(replace_text(path, REPLACEMENTS, args.apply) for path in text_files())
    changed += replace_text(ROOT / "build.gradle", ROOT_REPLACEMENTS, args.apply)
    changed += replace_text(ROOT / "gradle.properties", (), args.apply)
    moved = package_moves(args.apply)
    mode = "APPLIED" if args.apply else "DRY-RUN"
    print(f"{mode}: {changed} text files, {moved} package trees")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
