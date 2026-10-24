#!/usr/bin/env python3
"""Kora 1.x -> 2.x typed resilient specs. Import-bound; collision-preflighted."""
from __future__ import annotations
import re
import migration_common as common

KINDS = {'Timeout': ('Timeout', 'Timeouter', 'io.koraframework.resilient.timeout.Timeouter', 'io.koraframework.resilient.timeout.annotation.TimeoutSpec', 'timeout'), 'Retryable': ('Retry', 'Retry', 'io.koraframework.resilient.retry.Retry', 'io.koraframework.resilient.retry.annotation.RetrySpec', 'retry'), 'CircuitBreakable': ('CircuitBreaker', 'CircuitBreaker', 'io.koraframework.resilient.circuitbreaker.CircuitBreaker', 'io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec', 'circuitbreaker'), 'RateLimited': ('RateLimited', 'RateLimiter', 'io.koraframework.resilient.ratelimiter.RateLimiter', 'io.koraframework.resilient.ratelimiter.annotation.RateLimiterSpec', 'ratelimiter')}

JAVA_TEMPLATE = 'package {package};\n\nimport {base};\nimport {spec};\n\n@{spec_simple}("resilient.{section}.{name}")\npublic interface {interface} extends {base_simple} {{\n\n}}\n'

KOTLIN_TEMPLATE = 'package {package}\n\nimport {base}\nimport {spec}\n\n@{spec_simple}("resilient.{section}.{name}")\ninterface {interface} : {base_simple}\n'

def interface_name(name, suffix):
    stem = ''.join(word[:1].upper() + word[1:] for word in re.split(r'[^A-Za-z0-9]+', name) if word)
    return ('Spec' if not stem or stem[0].isdigit() else '') + stem + suffix


def migrate_source(path, original, config_prefix="resilient", spec_prefix=""):
    text = common.replace_code(original, 'ru.tinkoff.kora', 'io.koraframework')
    mask = common.code_mask(text)
    package = re.search(r'(?m)^\s*package\s+([\w.]+)', mask.lstrip('\ufeff'))
    if not package:
        return original, {}, ['Missing package declaration; source skipped']
    generated, reviews = {}, []
    for annotation, (legacy, suffix, base, spec, section) in KINDS.items():
        owner = spec.rsplit('.', 1)[0] + '.' + legacy
        if not common.has_import(text, owner):
            if re.search(r'@(?:[\w.]+\.)?' + legacy + r'\s*\(', mask):
                reviews.append(f'@{legacy}: explicit Kora import required; wildcard/alias/FQ annotation left unchanged')
            continue
        pattern = re.compile(r'@' + legacy + r'\(\s*"([A-Za-z0-9_.-]+)"\s*\)')
        matches = [match for match in pattern.finditer(text)
                   if mask[match.start():match.start()+len(legacy)+1] == '@'+legacy]
        if re.search(r'@'+legacy+r'\s*\(', mask) and not matches:
            reviews.append(f'@{legacy}: unsupported arguments; migrate typed spec manually')
        for match in reversed(matches):
            name = match.group(1)
            interface = spec_prefix + interface_name(name, suffix)
            content = (KOTLIN_TEMPLATE if path.suffix == '.kt' else JAVA_TEMPLATE).format(
                package=package.group(1), base=base, spec=spec, spec_simple=spec.rsplit('.',1)[1],
                base_simple=base.rsplit('.',1)[1], section=section, name=name, interface=interface)
            content = content.replace('resilient.' + section + '.' + name, config_prefix + '.' + section + '.' + name)
            if '\r\n' in original:
                content = content.replace('\n', '\r\n')
            target = path.parent / (interface + path.suffix)
            if target in generated and generated[target] != content:
                raise ValueError(f'Normalized specification-name collision: {target}')
            generated[target] = content
            literal = '::class' if path.suffix == '.kt' else '.class'
            text = text[:match.start()] + '@'+annotation+'('+interface+literal+')' + text[match.end():]
        if matches and legacy != annotation:
            text = common.replace_code(text, owner, spec.rsplit('.',1)[0]+'.'+annotation)
        mask = common.code_mask(text)
    return text, generated, reviews


def main():
    parser = common.parser(__doc__)
    parser.add_argument('--config-prefix', default='resilient', help='Root for generated spec paths')
    parser.add_argument('--spec-prefix', default='', help='Java/Kotlin identifier prefix for generated types')
    args = parser.parse_args()
    if not re.fullmatch(r'[A-Za-z0-9_.-]+', args.config_prefix):
        parser.error('--config-prefix must be a literal dotted configuration path')
    if args.spec_prefix and not re.fullmatch(r'[A-Za-z_][A-Za-z0-9_]*', args.spec_prefix):
        parser.error('--spec-prefix must be an identifier prefix')
    root = args.project_root.resolve()
    plans, reviews = [], []
    for path in common.files(args, {'.java', '.kt'}):
        old = common.read(path)
        new, generated, notices = migrate_source(path, old, args.config_prefix, args.spec_prefix)
        plans.append((path, path, old, new))
        for target, content in generated.items():
            plans.append((None, target, '', content))
        reviews.extend(f'{path.relative_to(root)}: {notice}' for notice in notices)
    return common.execute(args, plans, reviews)


if __name__ == '__main__':
    common.run(main)
