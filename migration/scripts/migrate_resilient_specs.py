#!/usr/bin/env python3
"""Kora 1.x -> 2.0: turn string-named resilient annotations into typed specifications.

In Kora 1.x a resilient aspect was selected by a name that also served as the configuration
path suffix:

    @Timeout("pet")
    @CircuitBreakable("pet")
    Pet findById(long id);

In Kora 2.0 the aspect annotation takes a *type*, and that type is an interface which declares
where its configuration lives:

    @TimeoutSpec("resilient.timeout.pet")
    public interface PetTimeouter extends Timeouter {}

    @Timeout(PetTimeouter.class)
    @CircuitBreakable(PetCircuitBreaker.class)
    Pet findById(long id);

Without the change the code does not compile:

    error: incompatible types: String cannot be converted to Class<? extends Timeouter>

What this script does
---------------------
For every `@Timeout("x")`, `@Retryable("x")`, `@CircuitBreakable("x")` and `@RateLimited("x")`
found in Java or Kotlin sources it

  1. generates the specification interface next to the annotated file, in the same package,
     pointing at the configuration path `resilient.<kind>.<x>` — which is exactly where the
     configuration already lives, so no configuration edits are needed;
  2. rewrites the annotation argument to reference that interface.

Naming follows the reference module `examples/java/kora-java-crud`: `pet` becomes `PetTimeouter`,
`PetCircuitBreaker`, `PetRetry`, `PetRateLimiter`.

Dry-run by default; pass --apply to write. Idempotent: an annotation that already takes a type
is left alone, and an existing specification file is never overwritten.

Usage:
    python migration/scripts/migrate_resilient_specs.py [--apply] [PATH ...]

PATH defaults to the `examples` and `guides` directories of this repository.

Limitations:
    - `@Fallback` is not handled: Kora 2.0 has no `FallbackSpec`, so that annotation needs a
      decision per call site rather than a mechanical rewrite.
    - The configuration *shape* of a circuit breaker also changed in 2.0 (`slidingWindowSize`
      became `type` + `countBased.windowSize`); this script only touches code, never configuration.
    - The package is taken from the `package` statement of the annotated file. A file without one
      (default package) is reported and skipped.
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SCAN = (ROOT / "examples", ROOT / "guides")
SKIP_PARTS = {"build", ".gradle", ".git", "agents-md"}

# 2.0 annotation -> (1.x annotation name, suffix of the generated interface, base type,
#                    spec annotation, config section)
# `@CircuitBreaker` and `@Retry` were renamed in 2.0; `@Timeout` kept its name.
KINDS = {
    "Timeout": ("Timeout", "Timeouter", "io.koraframework.resilient.timeout.Timeouter",
                "io.koraframework.resilient.timeout.annotation.TimeoutSpec", "timeout"),
    "Retryable": ("Retry", "Retry", "io.koraframework.resilient.retry.Retry",
                  "io.koraframework.resilient.retry.annotation.RetrySpec", "retry"),
    "CircuitBreakable": ("CircuitBreaker", "CircuitBreaker", "io.koraframework.resilient.circuitbreaker.CircuitBreaker",
                         "io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec", "circuitbreaker"),
    "RateLimited": ("RateLimited", "RateLimiter", "io.koraframework.resilient.ratelimiter.RateLimiter",
                    "io.koraframework.resilient.ratelimiter.annotation.RateLimiterSpec", "ratelimiter"),
}

PACKAGE = re.compile(r"^\s*package\s+([\w.]+)", re.M)

JAVA_TEMPLATE = """package {package};

import {base};
import {spec};

@{spec_simple}("resilient.{section}.{name}")
public interface {interface} extends {base_simple} {{

}}
"""

KOTLIN_TEMPLATE = """package {package}

import {base}
import {spec}

@{spec_simple}("resilient.{section}.{name}")
interface {interface} : {base_simple}
"""


def source_files(scan_roots):
    for scan_root in scan_roots:
        if scan_root.is_file():
            yield scan_root
            continue
        for path in scan_root.rglob("*"):
            if path.suffix in {".java", ".kt"} and path.is_file() and not SKIP_PARTS.intersection(path.parts):
                yield path


def interface_name(name: str, suffix: str) -> str:
    # `pet` -> `PetTimeouter`, `pet-api` -> `PetApiTimeouter`
    parts = re.split(r"[^A-Za-z0-9]+", name)
    return "".join(p[:1].upper() + p[1:] for p in parts if p) + suffix


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true", help="write changes; default is dry-run")
    parser.add_argument("paths", nargs="*", type=Path, help="files or directories to scan")
    args = parser.parse_args()

    # resolved so that paths given relative to the working directory stay under ROOT when reported
    scan_roots = [path.resolve() for path in (args.paths or list(DEFAULT_SCAN))]

    changed_files = 0
    created_files = 0
    warnings = []

    for path in source_files(scan_roots):
        original = path.read_text(encoding="utf-8")
        text = original
        kotlin = path.suffix == ".kt"
        specs = {}

        for annotation, (legacy, suffix, base, spec, section) in KINDS.items():
            pattern = re.compile(rf"@{legacy}\(\s*\"([^\"]+)\"\s*\)")
            for match in pattern.finditer(text):
                name = match.group(1)
                specs[interface_name(name, suffix)] = (name, base, spec, section)
            if not pattern.search(text):
                continue
            reference = "::class" if kotlin else ".class"
            text = pattern.sub(
                lambda m: f"@{annotation}({interface_name(m.group(1), suffix)}{reference})", text)
            if legacy != annotation:
                # the annotation itself was renamed, so its import has to follow
                legacy_import = spec.rsplit(".", 1)[0] + "." + legacy
                text = text.replace(legacy_import, spec.rsplit(".", 1)[0] + "." + annotation)

        if not specs:
            continue

        # some sources carry a BOM, which `\s` does not match
        package_match = PACKAGE.search(original.lstrip("﻿"))
        if package_match is None:
            warnings.append(f"{path.relative_to(ROOT)}: no package statement, skipped")
            continue
        package = package_match.group(1)

        if text != original:
            changed_files += 1
            print(f"CHANGE {path.relative_to(ROOT)}: {', '.join(sorted(specs))}")
            if args.apply:
                path.write_text(text, encoding="utf-8")

        for interface, (name, base, spec, section) in specs.items():
            target = path.parent / f"{interface}{path.suffix}"
            if target.exists():
                continue
            template = KOTLIN_TEMPLATE if kotlin else JAVA_TEMPLATE
            content = template.format(
                package=package,
                base=base,
                spec=spec,
                spec_simple=spec.rsplit(".", 1)[1],
                base_simple=base.rsplit(".", 1)[1],
                section=section,
                name=name,
                interface=interface,
            )
            created_files += 1
            print(f"CREATE {target.relative_to(ROOT)}")
            if args.apply:
                target.write_text(content, encoding="utf-8")

    for warning in warnings:
        print(f"WARN   {warning}", file=sys.stderr)

    mode = "APPLIED" if args.apply else "DRY-RUN"
    print(f"{mode}: {changed_files} file(s) changed, {created_files} specification(s) created")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
