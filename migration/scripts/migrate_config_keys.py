#!/usr/bin/env python3
"""Kora 1.x -> 2.x configuration keys, scoped by explicit configuration paths.

Handles simple multiline HOCON, YAML mappings and dotted .properties keys.
Inline HOCON objects, substitutions as keys, YAML aliases/flow collections and
source-embedded configuration are reported for manual review, never guessed.
"""
from __future__ import annotations
import re
import migration_common as common

HTTP = {'publicApiHttpPort': 'port', 'privateApiHttpPort': 'system.port',
        'privateApiHttpReadinessPath': 'system.readinessPath',
        'privateApiHttpLivenessPath': 'system.livenessPath',
        'privateApiHttpMetricsPath': 'system.metricsPath'}


def entries(text, yaml):
    """Yield (line index, full path, local key, indentation, value, opening block)."""
    stack = []
    for index, line in enumerate(text.splitlines(True)):
        stripped = line.strip()
        if not stripped or stripped.startswith(('#', '//', '!')):
            continue
        indent = line[:len(line)-len(line.lstrip())]
        if yaml:
            while stack and len(indent) <= stack[-1][0]:
                stack.pop()
        else:
            if re.fullmatch(r'\},?\s*(?:(?://|#).*)?', stripped):
                if stack:
                    stack.pop()
                continue
        match = re.match(r'([A-Za-z_][\w.-]*)\s*(\{|[:=])\s*(.*?)(?:\r?\n)?$', line.lstrip())
        if not match:
            if yaml and stripped.startswith('-'):
                stack.append((len(indent), '__unsupported_list__'))
            continue
        key, separator, value = match.groups()
        # Inline structures are not parsed by this deliberately small reader.
        if separator == '{' and value.strip() or any(c in common.code_mask(value) for c in '{}'):
            if '{' in common.code_mask(value) and '}' not in common.code_mask(value):
                stack.append((len(indent), '__unsupported_object__'))
            continue
        prefix = stack[-1][1] if stack else ''
        full = prefix + '.' + key if prefix else key
        block = separator == '{' or yaml and separator == ':' and (not value.strip() or value.lstrip().startswith('#'))
        yield index, full, key, indent, value, block
        if block or yaml:
            stack.append((len(indent), full))


def migrate_text(path, text, args):
    yaml = path.suffix in {'.yaml', '.yml'}
    if yaml and re.search(r'(?m)(?:^|\s)(?:[&*][\w-]+|<<:)', text):
        return text, ['YAML anchors/aliases/merge keys: migrate with a YAML-aware tool']
    newline = '\r\n' if '\r\n' in text else '\n'
    lines = text.splitlines(True)
    items = list(entries(text, yaml))
    paths = {item[1] for item in items}
    edits, system, reviews = {}, [], []
    for index, full, key, indent, value, block in items:
        parent = full.rsplit('.', 1)[0] if '.' in full else ''
        leaf = full.rsplit('.', 1)[-1]
        replacement = None
        if parent == args.http_server_path and leaf in HTTP:
            replacement = HTTP[leaf]
            if replacement.startswith('system.') and yaml:
                if args.http_server_path + '.system' in paths:
                    reviews.append('Existing HTTP system block: merge legacy privateApi keys manually')
                else:
                    system.append((index, indent, replacement.split('.', 1)[1], value))
                continue
        if parent == args.openapi_path and leaf == 'file' and not block:
            if args.openapi_path + '.files' in paths:
                reviews.append('Both OpenAPI file and files exist: merge manually')
                continue
            # Only a simple string literal is mechanically turned into a one-element list.
            if not re.fullmatch(r'"(?:\\.|[^"\\])*"\s*', value):
                reviews.append('OpenAPI file value is not a simple quoted string: convert to files list manually')
                continue
            replacement = 'files'
            value = '[' + value.strip() + ']'
            edits[index] = indent + (key[:len(key)-len(leaf)] if '.' in key else '') + 'files' + (': ' if yaml else ' = ') + value + newline
            continue
        if full == args.openapi_path + '.rapidoc' or full.startswith(args.openapi_path + '.rapidoc.'):
            if 'rapidoc' in key:
                edits[index] = lines[index].replace(key, key.replace('rapidoc', 'scalar', 1), 1)
            continue
        if leaf == 'slidingWindowSize' and parent in args.circuit_breaker_path:
            if parent + '.type' in paths or parent + '.countBased' in paths or parent + '.countBased.windowSize' in paths:
                reviews.append(f'{parent}: existing type/countBased requires manual merge')
            else:
                prefix = parent + '.' if '.' in key else ''
                if yaml:
                    edits[index] = indent + 'type: FIXED_WINDOW' + newline + indent + 'countBased:' + newline + indent + '  windowSize: ' + value + newline
                else:
                    edits[index] = indent + prefix + 'type = FIXED_WINDOW' + newline + indent + prefix + 'countBased.windowSize = ' + value + newline
            continue
        if replacement:
            target = parent + '.' + replacement
            if target in paths:
                reviews.append(f'{full}: target {target} already exists; merge manually')
                continue
            # Replace local key only, retain value/comments and formatting.
            local = replacement if '.' not in key else key[:len(key)-len(leaf)] + replacement
            edits[index] = lines[index].replace(key, local, 1)
    if system:
        first = system[0][0]
        body = system[0][1] + 'system:' + newline
        for index, indent, key, value in system:
            body += indent + '  ' + key + ': ' + value + newline
            edits[index] = ''
        edits[first] = body
    for token, notice in (
            ('jdbcUrl', 'Review JDBC factory config path; custom db paths are preserved'),
            ('slidingWindowSize', 'Set --circuit-breaker-path for each owned legacy circuit-breaker block'),
            ('singlePartUploadLimit', 'HEAD S3 removed upload.singlePartUploadLimit; review only S3-owned config'),
            ('{', 'Inline HOCON objects are left for manual review if not simple multiline blocks')):
        if token in text and (token != '{' or any('{' in line and '}' in line for line in lines)):
            reviews.append(notice)
    return ''.join(edits.get(i, line) for i, line in enumerate(lines)), reviews


def main():
    parser = common.parser(__doc__)
    parser.add_argument('--http-server-path', default='httpServer')
    parser.add_argument('--openapi-path', default='openapi.management')
    parser.add_argument('--circuit-breaker-path', action='append', default=[])
    args = parser.parse_args()
    root = args.project_root.resolve()
    plans, reviews = [], []
    for path in common.files(args, {'.conf', '.yaml', '.yml', '.properties'}):
        old = common.read(path)
        new, notices = migrate_text(path, old, args)
        plans.append((path, path, old, new))
        reviews.extend(f'{path.relative_to(root)}: {notice}' for notice in notices)
    return common.execute(args, plans, reviews)


if __name__ == '__main__':
    common.run(main)
