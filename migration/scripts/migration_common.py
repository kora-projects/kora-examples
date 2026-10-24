"""Shared project scope, text IO and preflight for portable migration commands."""
from __future__ import annotations

import argparse
import difflib
import fnmatch
import os
import re
import stat
from pathlib import Path

TOOL_ROOT = Path(__file__).resolve().parent.parent


def linked(path):
    return path.is_symlink() or bool(getattr(path.lstat(), 'st_file_attributes', 0) & getattr(stat, 'FILE_ATTRIBUTE_REPARSE_POINT', 0))


SKIP = {'.git', '.gradle', '.idea', '.kotlin', 'build', 'target', 'out',
        'node_modules', '__pycache__', '.venv', 'venv'}


def parser(description):
    result = argparse.ArgumentParser(description=description)
    result.add_argument('--project-root', type=Path, default=Path.cwd(),
                        help='Target project root (default: current working directory)')
    result.add_argument('--apply', action='store_true', help='Write the reviewed plan; default: dry-run')
    result.add_argument('--diff', action='store_true', help='Print unified diffs')
    result.add_argument('--exclude', action='append', default=[], metavar='GLOB',
                        help='Exclude a root-relative path/glob (repeatable)')
    result.add_argument('paths', nargs='*', type=Path,
                        help='Files/directories relative to project root; default: entire project')
    return result


def files(args, suffixes):
    root = args.project_root.resolve()
    if not root.is_dir():
        raise ValueError(f'Project root is not a directory: {root}')
    roots = args.paths or [root]
    found = set()

    def allowed(path):
        if linked(path) or TOOL_ROOT != root and path.is_relative_to(TOOL_ROOT):
            return False
        relative = path.relative_to(root)
        return not SKIP.intersection(relative.parts) and not any(
            fnmatch.fnmatch(relative.as_posix(), pattern) or
            any(fnmatch.fnmatch('/'.join(relative.parts[:i]), pattern)
                for i in range(1, len(relative.parts) + 1)) for pattern in args.exclude)

    for entry in roots:
        entry = entry if entry.is_absolute() else root / entry
        # Check lexical and resolved paths: explicit symlinks must not bypass scope.
        if entry.exists() and linked(entry):
            continue
        entry = entry.resolve()
        if not entry.is_relative_to(root):
            raise ValueError(f'Path escapes project root: {entry}')
        if not entry.exists():
            raise ValueError(f'Path does not exist: {entry}')
        if entry.is_file():
            if allowed(entry) and entry.suffix in suffixes:
                found.add(entry)
            continue
        for directory, dirs, names in os.walk(entry, followlinks=False):
            current = Path(directory)
            dirs[:] = [name for name in dirs if allowed(current / name)]
            for name in names:
                path = current / name
                if path.suffix in suffixes and allowed(path):
                    found.add(path)
    return sorted(found)


def read(path):
    with path.open(encoding='utf-8', newline='') as stream:
        return stream.read()


def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open('w', encoding='utf-8', newline='') as stream:
        stream.write(text)


def code_mask(text):
    """Keep offsets; hide comments and Java/Kotlin strings (including nested comments)."""
    result = list(text)
    index = 0

    def hide(start, end):
        for offset in range(start, end):
            if text[offset] not in '\r\n':
                result[offset] = ' '

    while index < len(text):
        start = index
        if text.startswith('//', index):
            end = text.find('\n', index)
            index = len(text) if end < 0 else end
        elif text.startswith('/*', index):
            index += 2
            depth = 1
            while index < len(text) and depth:
                if text.startswith('/*', index):
                    depth += 1
                    index += 2
                elif text.startswith('*/', index):
                    depth -= 1
                    index += 2
                else:
                    index += 1
        elif text.startswith('"""', index):
            index += 3
            while index < len(text):
                if text.startswith('"""', index) and (index == 0 or text[index-1] != chr(92)):
                    index += 3
                    break
                index += 1
        elif text[index] in ('"', "'"):
            quote = text[index]
            index += 1
            while index < len(text):
                if text[index] == chr(92):
                    index += 2
                elif text[index] == quote:
                    index += 1
                    break
                else:
                    index += 1
            index = min(index, len(text))
        else:
            index += 1
            continue
        hide(start, index)
    return ''.join(result)


def replace_code(text, old, new):
    matches = list(re.finditer(r'(?<![\w$])' + re.escape(old) + r'(?![\w$])', code_mask(text)))
    for match in reversed(matches):
        text = text[:match.start()] + new + text[match.end():]
    return text


def has_import(text, fqn):
    return bool(re.search(r'(?m)^[\s\ufeff]*import\s+' + re.escape(fqn) + r'\s*;?\s*$', code_mask(text)))


def execute(args, plans, reviews=()):
    """plans: (old path or None, target path, original text, new text).

    Validate all targets before any write. A generated file is reused only if identical.
    """
    root = args.project_root.resolve()
    unique = {}
    for source, target, old, new in plans:
        if not target.resolve().is_relative_to(root):
            raise ValueError(f'Target escapes project root: {target}')
        for ancestor in (target, *target.parents):
            if ancestor == root:
                break
            if ancestor.exists() and linked(ancestor):
                raise ValueError(f'Target uses a link/reparse point: {target}')
        if target in unique and unique[target][3] != new:
            raise ValueError(f'Conflicting planned output: {target}')
        if source != target and target.exists() and read(target) != new:
            raise ValueError(f'Target collision (no files written): {target}')
        unique[target] = (source, target, old, new)
    changed = 0
    for source, target, old, new in unique.values():
        if source == target and old == new or source is None and target.exists():
            continue
        changed += 1
        label = target.relative_to(root).as_posix()
        print(f'{"CREATE" if source is None else "MOVE" if source != target else "CHANGE"} {label}')
        if args.diff:
            print(''.join(difflib.unified_diff(old.splitlines(True), new.splitlines(True),
                  fromfile=str(source or '/dev/null'), tofile=str(target))), end='')
        if args.apply:
            write(target, new)
            if source is not None and source != target:
                source.unlink()
    for review in sorted(set(reviews)):
        print(f'REVIEW {review}')
    print(f'{"APPLIED" if args.apply else "DRY-RUN"}: {changed} file(s), {len(set(reviews))} review item(s)')
    return 0


def run(main):
    try:
        raise SystemExit(main())
    except (ValueError, UnicodeError, OSError) as error:
        raise SystemExit(f'ERROR: {error}') from error
