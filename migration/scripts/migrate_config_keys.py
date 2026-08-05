#!/usr/bin/env python3
"""Kora 1.x -> 2.0: migrate configuration keys and section names.

HTTP server
-----------
In Kora 1.x the public and the private (system) HTTP servers were configured by flat keys
inside the `httpServer` section. In Kora 2.0 the system server has its own nested section
`httpServer.system`, and the public server uses plain `port`.

    publicApiHttpPort           -> port
    privateApiHttpPort          -> system.port
    privateApiHttpReadinessPath -> system.readinessPath
    privateApiHttpLivenessPath  -> system.livenessPath
    privateApiHttpMetricsPath   -> system.metricsPath

Why this matters beyond cosmetics: `SystemHttpServerConfig extends HttpServerConfig`, so the
system server inherits `port()` = 8080. While `privateApiHttpPort` stays unrecognised, BOTH
servers bind to 8080 and the application dies on startup with

    HTTP server 'kora-undertow-system' (Undertow) failed to start on port '8080': port is already in use

JDBC section
------------
The JDBC datasource section was renamed: `db` -> `jdbc`. In 2.0 `JdbcDatabaseModule` wires
`new JdbcDatabaseFactoryModule("jdbc")`, so a config still using `db` leaves every value unset and
the application fails on startup (or in tests) with

    ConfigValueException: Config expected value, but got null at path: 'ROOT.jdbc.username'

Only a top-level `db` block is renamed, and only in files that actually contain `jdbcUrl`, so
unrelated `db` sections of other integrations are left alone.

Dry-run by default; pass --apply to write. Idempotent: running it twice changes nothing.

Usage:
    python migration/scripts/migrate_config_keys.py [--apply] [PATH ...]

PATH defaults to the `examples` and `guides` directories of this repository.

Limitations:
    - Only HOCON/YAML/properties-like `key = value` and `key: value` forms are handled.
    - A key is rewritten only when it is not already nested under `system`; the script does not
      reformat or reorder the section, it only renames keys in place.
    - `dataApiHttpPort` and other non-Kora keys are left untouched and reported, because their
      intent is application-specific.
    - The `db` -> `jdbc` rule keys off a top-level `db {` / `db:` line; a JDBC section nested
      somewhere else has to be migrated by hand.
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SCAN = (ROOT / "examples", ROOT / "guides")
SUFFIXES = {".conf", ".yaml", ".yml", ".properties"}
SKIP_PARTS = {"build", ".gradle", ".git", "agents-md"}

RENAMES = (
    ("publicApiHttpPort", "port"),
    ("privateApiHttpPort", "system.port"),
    ("privateApiHttpReadinessPath", "system.readinessPath"),
    ("privateApiHttpLivenessPath", "system.livenessPath"),
    ("privateApiHttpMetricsPath", "system.metricsPath"),
)

UNKNOWN_KEYS = ("dataApiHttpPort",)


def config_files(scan_roots):
    for scan_root in scan_roots:
        if scan_root.is_file():
            yield scan_root
            continue
        for path in scan_root.rglob("*"):
            if path.is_file() and path.suffix in SUFFIXES and not SKIP_PARTS.intersection(path.parts):
                yield path


def migrate_text(text: str) -> tuple[str, list[str]]:
    changes = []
    for old, new in RENAMES:
        # `key = value` / `key: value`, keeping the original indentation and separator
        pattern = re.compile(rf"^(\s*){re.escape(old)}(\s*[:=])", re.M)
        text, count = pattern.subn(rf"\g<1>{new}\g<2>", text)
        if count:
            changes.append(f"{old} -> {new} ({count})")

    # `db` section is JDBC only when it configures a datasource
    if "jdbcUrl" in text:
        text, count = re.subn(r"^db(\s*[{:])", r"jdbc\g<1>", text, flags=re.M)
        if count:
            changes.append(f"db -> jdbc section ({count})")

    return text, changes


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true", help="write changes; default is dry-run")
    parser.add_argument("paths", nargs="*", type=Path, help="files or directories to scan")
    args = parser.parse_args()

    scan_roots = args.paths or list(DEFAULT_SCAN)

    changed_files = 0
    warned = []
    for path in config_files(scan_roots):
        original = path.read_text(encoding="utf-8")
        updated, changes = migrate_text(original)

        for unknown in UNKNOWN_KEYS:
            if re.search(rf"^\s*{re.escape(unknown)}\s*[:=]", original, re.M):
                warned.append(f"{path.relative_to(ROOT)}: {unknown} is not a Kora 2.0 key, left untouched")

        if updated == original:
            continue
        changed_files += 1
        print(f"CHANGE {path.relative_to(ROOT)}: {', '.join(changes)}")
        if args.apply:
            path.write_text(updated, encoding="utf-8")

    for warning in warned:
        print(f"WARN   {warning}", file=sys.stderr)

    mode = "APPLIED" if args.apply else "DRY-RUN"
    print(f"{mode}: {changed_files} file(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
