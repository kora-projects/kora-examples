#!/usr/bin/env python3
"""Kora 1.x -> 2.0: mechanical API renames that OpenRewrite does not cover.

Rules
-----
1. JSON module rename:      JsonCommonModule -> JsonModule
2. Repository executor: `<db>Repository#get<Db>ConnectionFactory()` became `executor()`,
                            returning the `<Db>Executor` that still carries `inTx(...)`.
3. JSON unchecked methods:  the `*Unchecked` variants are gone, the plain ones no longer
                            declare checked exceptions:
                                JsonWriter.toStringUnchecked(x)    -> toString(x)
                                JsonWriter.toByteArrayUnchecked(x) -> toByteArray(x)
                                JsonReader.readUnchecked(x)        -> read(x)
4. Config extraction contract: the runtime interface was renamed and moved, and its method
   now has an explicit "or throw" variant:
                                io.koraframework.config.common.extractor.ConfigMapper<T>
                             -> io.koraframework.config.common.mapper.ConfigValueMapper<T>
                                extractor.extract(value) -> mapper.mapOrThrow(value)
   The `@ConfigMapper` *annotation* keeps its name and lives in
   `io.koraframework.config.common.annotation` - it must not be renamed, which is why the
   rule keys off the old package and off the generic parameter.
5. Fallback lost its name: 2.0 has no FallbackSpec and no named fallback configuration,
   so the `value` attribute is gone and only `method` remains:
                                @Fallback(value = "default", method = "f()") -> @Fallback(method = "f()")
6. JSpecify placement (Java only): JSpecify annotations are type-use, so on a qualified
   nested type they must sit right before the simple name:
                                @Nullable Entity.FieldType -> Entity.@Nullable FieldType
   javac itself suggests this form:
       error: type annotation @org.jspecify.annotations.Nullable is not expected here
         (to annotate a qualified type, write Entity.@org.jspecify.annotations.Nullable FieldType)

Dry-run by default; pass --apply to write. Idempotent.

Usage:
    python migration/scripts/migrate_api_renames.py [--apply] [PATH ...]

Limitations
-----------
- Rule 6 only rewrites `@Nullable` / `@NonNull` / `@NullMarked` immediately followed by a
  dotted type whose every segment starts with an upper-case letter (a nested type). Package
  qualified names (`java.lang.String`) are intentionally not touched, since the correct
  placement there depends on the surrounding declaration.
- Rule 6 is skipped for Kotlin: there nullability is expressed by the type (`T?`), and the
  annotation is removed rather than moved.
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SCAN = (ROOT / "examples", ROOT / "guides")
SKIP_PARTS = {"build", ".gradle", ".git", "agents-md"}

TEXT_RENAMES = (
    ("JsonCommonModule", "JsonModule"),
    ("toStringUnchecked(", "toString("),
    ("toByteArrayUnchecked(", "toByteArray("),
    ("readUnchecked(", "read("),
    # the repository no longer exposes a connection factory, it exposes the executor
    ("getJdbcConnectionFactory()", "executor()"),
    ("getR2dbcConnectionFactory()", "executor()"),
    ("getVertxConnectionFactory()", "executor()"),
    ("getCassandraConnectionFactory()", "executor()"),
)

# The old config extraction contract. Keyed off the old package so the `@ConfigMapper`
# annotation of 2.0, which shares the simple name, is never touched.
CONFIG_EXTRACTOR_IMPORT = "io.koraframework.config.common.extractor.ConfigMapper"
CONFIG_VALUE_MAPPER_IMPORT = "io.koraframework.config.common.mapper.ConfigValueMapper"

# @Fallback(value = "name", method = "f()")  ->  @Fallback(method = "f()")
FALLBACK_VALUE = re.compile(r'@Fallback\(\s*value\s*=\s*"[^"]*"\s*,\s*')

# @Nullable Outer.Inner  ->  Outer.@Nullable Inner
QUALIFIED_TYPE_ANNOTATION = re.compile(
    r"@(Nullable|NonNull|NullMarked)\s+((?:[A-Z][A-Za-z0-9_]*\.)+)([A-Z][A-Za-z0-9_]*)"
)


def source_files(scan_roots):
    for scan_root in scan_roots:
        if scan_root.is_file():
            yield scan_root
            continue
        for path in scan_root.rglob("*"):
            if path.suffix in {".java", ".kt"} and path.is_file() and not SKIP_PARTS.intersection(path.parts):
                yield path


def migrate_text(path: Path, text: str) -> tuple[str, list[str]]:
    changes = []
    for old, new in TEXT_RENAMES:
        if old in text:
            count = text.count(old)
            text = text.replace(old, new)
            changes.append(f"{old} -> {new} ({count})")

    if CONFIG_EXTRACTOR_IMPORT in text:
        text = text.replace(CONFIG_EXTRACTOR_IMPORT, CONFIG_VALUE_MAPPER_IMPORT)
        text, generics = re.subn(r"\bConfigMapper<", "ConfigValueMapper<", text)
        text, calls = re.subn(r"\.extract\(", ".mapOrThrow(", text)
        changes.append(f"ConfigMapper -> ConfigValueMapper ({generics}), extract -> mapOrThrow ({calls})")

    text, count = FALLBACK_VALUE.subn("@Fallback(", text)
    if count:
        changes.append(f"@Fallback value attribute removed ({count})")

    if path.suffix == ".java":
        text, count = QUALIFIED_TYPE_ANNOTATION.subn(r"\g<2>@\g<1> \g<3>", text)
        if count:
            changes.append(f"type-use annotation moved onto nested type ({count})")

    return text, changes


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true", help="write changes; default is dry-run")
    parser.add_argument("paths", nargs="*", type=Path, help="files or directories to scan")
    args = parser.parse_args()

    changed = 0
    for path in source_files(args.paths or list(DEFAULT_SCAN)):
        original = path.read_text(encoding="utf-8")
        updated, changes = migrate_text(path, original)
        if updated == original:
            continue
        changed += 1
        print(f"CHANGE {path.relative_to(ROOT)}: {', '.join(changes)}")
        if args.apply:
            path.write_text(updated, encoding="utf-8")

    print(f"{'APPLIED' if args.apply else 'DRY-RUN'}: {changed} file(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
