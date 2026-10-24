#!/usr/bin/env python3
"""Kora 1.x -> 2.x source API renames with import ownership checks."""
from __future__ import annotations
import re
import migration_common as common


def migrate_text(path, text):
    changes, reviews = [], []
    # This command also works before the package stage.
    text = common.replace_code(text, 'ru.tinkoff.kora', 'io.koraframework')
    for old, new in (
            ('io.koraframework.json.common.JsonCommonModule', 'io.koraframework.json.common.JsonModule'),
            ('io.koraframework.config.common.extractor.ConfigMapper', 'io.koraframework.config.common.mapper.ConfigValueMapper')):
        if common.has_import(text, old):
            text = common.replace_code(text, old, new)
            text = common.replace_code(text, old.rsplit('.', 1)[1], new.rsplit('.', 1)[1])
            changes.append(old + ' -> ' + new)
    if common.has_import(text, 'io.koraframework.resilient.fallback.annotation.Fallback'):
        pattern = re.compile(r'@Fallback\(\s*value\s*=\s*"(?:\\.|[^"\\])*"\s*,\s*(?=method\s*=)')
        mask = common.code_mask(text)
        for match in reversed(list(pattern.finditer(text))):
            if mask[match.start():match.start()+9] == '@Fallback':
                text = text[:match.start()] + '@Fallback(' + text[match.end():]
                changes.append('removed named fallback value')
    if path.suffix == '.java' and common.has_import(text, 'org.jspecify.annotations.Nullable'):
        pattern = r'@Nullable\s+((?:[A-Z][A-Za-z0-9_]*\.)+)([A-Z][A-Za-z0-9_]*)'
        for match in reversed(list(re.finditer(pattern, common.code_mask(text)))):
            text = text[:match.start()] + match.group(1) + '@Nullable ' + match.group(2) + text[match.end():]
            changes.append('moved Nullable to nested type')
    mask = common.code_mask(text)
    for pattern, message in (
            (r'\b(?:toStringUnchecked|toByteArrayUnchecked|readUnchecked)\s*\(', 'Resolve receiver type; Kora JSON unchecked methods become plain methods'),
            (r'\bget(?:Jdbc|R2dbc|Vertx|Cassandra)ConnectionFactory\s*\(', 'Resolve repository receiver; Kora accessors become executor()'),
            (r'\.extract\s*\(', 'Resolve receiver; ConfigValueMapper uses mapOrThrow(), unrelated extract() must remain'),
            (r'@Fallback\(', 'Review remaining Fallback arguments and exception semantics'),
            (r'import\s+.*\.\*', 'Wildcard imports require symbol/type review before simple-name changes'),
            (r'@(?:Nullable|Nonnull|NonNull)\b', 'Review nullability: no automatic global annotation removal')):
        if re.search(pattern, mask):
            reviews.append(message)
    return text, changes, reviews


def main():
    args = common.parser(__doc__).parse_args()
    plans, reviews = [], []
    root = args.project_root.resolve()
    for path in common.files(args, {'.java', '.kt'}):
        old = common.read(path)
        new, _, notices = migrate_text(path, old)
        plans.append((path, path, old, new))
        reviews.extend(f'{path.relative_to(root)}: {notice}' for notice in notices)
    return common.execute(args, plans, reviews)


if __name__ == '__main__':
    common.run(main)
