#!/usr/bin/env python3
"""Portable Kora 1.x -> 2.x package, build and source migration. Dry-run by default."""
from __future__ import annotations

import re
from pathlib import Path
import migration_common as common

TYPE_MOVES = (
    ('io.koraframework.logging.logback.ConsoleTextRecordEncoder', 'io.koraframework.logging.logback.text.ConsoleTextRecordEncoder'),
    ('io.koraframework.common.KoraApp', 'io.koraframework.common.annotation.KoraApp'),
    ('io.koraframework.common.Component', 'io.koraframework.common.annotation.Component'),
    ('io.koraframework.common.DefaultComponent', 'io.koraframework.common.annotation.DefaultComponent'),
    ('io.koraframework.common.KoraSubmodule', 'io.koraframework.common.annotation.KoraSubmodule'),
    ('io.koraframework.common.Mapping', 'io.koraframework.common.annotation.Mapping'),
    ('io.koraframework.common.Module', 'io.koraframework.common.annotation.Module'),
    ('io.koraframework.common.Tag', 'io.koraframework.common.annotation.Tag'),
    ('io.koraframework.http.server.undertow.UndertowHttpServerModule', 'io.koraframework.http.server.undertow.UndertowPublicHttpServerModule'),
    ('io.koraframework.http.server.common.HttpServerInterceptor', 'io.koraframework.http.server.common.interceptor.HttpServerInterceptor'),
    ('io.koraframework.http.server.common.HttpServerRequest', 'io.koraframework.http.server.common.request.HttpServerRequest'),
    ('io.koraframework.http.server.common.HttpServerResponseException', 'io.koraframework.http.server.common.response.HttpServerResponseException'),
    ('io.koraframework.http.server.common.HttpServerResponse', 'io.koraframework.http.server.common.response.HttpServerResponse'),
    ('io.koraframework.http.server.common.handler.HttpServerRequestHandlerImpl', 'io.koraframework.http.server.common.request.HttpServerRequestHandlerImpl'),
    ('io.koraframework.http.server.common.handler.HttpServerRequestHandler', 'io.koraframework.http.server.common.request.HttpServerRequestHandler'),
    ('io.koraframework.http.server.common.handler.HttpServerRequestMapper', 'io.koraframework.http.server.common.request.HttpServerRequestMapper'),
    ('io.koraframework.http.server.common.handler.HttpServerResponseMapper', 'io.koraframework.http.server.common.response.HttpServerResponseMapper'),
    ('io.koraframework.http.client.common.HttpClientDecoderException', 'io.koraframework.http.client.common.exception.HttpClientDecoderException'),
    ('io.koraframework.http.client.common.HttpClientResponseException', 'io.koraframework.http.client.common.exception.HttpClientResponseException'),
    ('io.koraframework.database.jdbc.EntityJdbc', 'io.koraframework.database.jdbc.annotation.EntityJdbc'),
    ('io.koraframework.common.util.ByteBufferPublisherInputStream', 'io.koraframework.common.util.ByteBufferInputStream'),
    ('io.koraframework.json.module.JsonModule', 'io.koraframework.json.common.JsonModule'),
    ('io.koraframework.cache.redis.RedisCacheModule', 'io.koraframework.cache.redis.lettuce.LettuceRedisCacheModule'),
    ('io.koraframework.experimental:s3-client-aws', 'io.koraframework:s3-client-aws'),
)

SUFFIXES = {'.java', '.kt', '.gradle', '.kts', '.properties', '.xml', '.toml',
            '.conf', '.yaml', '.yml', '.json', '.proto'}


def migrate_text(path, text, args):
    reviews = []
    source = path.suffix in {'.java', '.kt'}
    if source:
        text = common.replace_code(text, 'ru.tinkoff.kora', 'io.koraframework')
        for old, new in TYPE_MOVES:
            imported = common.has_import(text, old)
            text = common.replace_code(text, old, new)
            if imported and old.rsplit('.', 1)[1] != new.rsplit('.', 1)[1]:
                text = common.replace_code(text, old.rsplit('.', 1)[1], new.rsplit('.', 1)[1])
        old = 'io.koraframework.config.common.annotation.ConfigValueExtractor'
        if common.has_import(text, old):
            text = common.replace_code(text, old, 'io.koraframework.config.common.annotation.ConfigMapper')
            text = common.replace_code(text, 'ConfigValueExtractor', 'ConfigMapper')
        if common.has_import(text, 'io.koraframework.cache.annotation.CacheInvalidate'):
            pattern = r'@CacheInvalidate\(\s*value\s*=\s*([\w.]+(?:\.class|::class))\s*,\s*invalidateAll\s*=\s*true\s*\)'
            masked = common.code_mask(text)
            matches = list(re.finditer(pattern, masked))
            for match in reversed(matches):
                text = text[:match.start()] + '@CacheInvalidateAll(' + match.group(1) + ')' + text[match.end():]
            if matches and not common.has_import(text, 'io.koraframework.cache.annotation.CacheInvalidateAll'):
                newline = '\r\n' if '\r\n' in text else '\n'
                text = re.sub(r'(?m)^([ \t\ufeff]*import io\.koraframework\.cache\.annotation\.CacheInvalidate;?)',
                              lambda m: m.group(1) + newline + 'import io.koraframework.cache.annotation.CacheInvalidateAll' + (';' if path.suffix == '.java' else ''), text, count=1)

        masked = common.code_mask(text)
        for token, reason in (
                ('suspend', 'Review Kora contract suspend removal and coroutine call chains'),
                ('@Nullable', 'Review nullable type-use positions; Kotlin uses T?'),
                ('@Nonnull', 'Review nullability; annotations are not deleted automatically'),
                ('@NonNull', 'Review nullability; annotations are not deleted automatically'),
                ('invalidateAll', 'Review remaining CacheInvalidate arguments'),
                ('parameters =', 'Review Kora annotation parameters -> args if applicable')):
            if token in masked:
                reviews.append(reason)
        if 'ru.tinkoff.kora' in text:
            reviews.append('Legacy namespace remains in comments/strings; review runtime FQCN references')
        return text, reviews

    # Resources/build files can contain FQCNs as data. Never replace unrelated simple names.
    text = re.sub(r'(?<![\w.])ru\.tinkoff\.kora(?=[.:"\s<]|$)', 'io.koraframework', text)
    for old, new in TYPE_MOVES:
        text = re.sub(r'(?<![\w.])' + re.escape(old) + r'(?![\w])', lambda _: new, text)
    build = path.suffix in {'.gradle', '.kts', '.toml'} or path.name in {'pom.xml', 'gradle.properties'}
    if not build:
        return text, reviews
    artifacts = {'kora-parent': 'kora-bom', 'json-module': 'json-common', 'cache-redis': 'cache-redis-lettuce'}
    text = re.sub(r'io\.koraframework\.experimental:s3-client-aws\b', 'io.koraframework:s3-client-aws', text)
    for old, new in artifacts.items():
        text = re.sub(r'(io\.koraframework:)' + re.escape(old) + r'(?=[:"\s\'])', r'\g<1>' + new, text)
    # POM dependencies: rename artifacts only in blocks owned by the Kora group.
    if path.name == 'pom.xml':
        def dependency(match):
            block = match.group()
            if re.search(r'<groupId>\s*io\.koraframework\s*</groupId>', block):
                for old, new in artifacts.items():
                    block = re.sub(r'<artifactId>\s*' + old + r'\s*</artifactId>', '<artifactId>' + new + '</artifactId>', block)
                block = re.sub(r'<version>\s*(?:1\.[\w.+-]+|2\.0\.0\.RC1)\s*</version>', '<version>' + args.target_version + '</version>', block)
            return block
        text = re.sub(r'<dependency\b[^>]*>[\s\S]*?</dependency>', dependency, text)
    if path.suffix == '.toml' and 'io.koraframework' in text:
        # Coordinates are handled above; artifact = entries depend on their group/table.
        reviews.append('Review version-catalog aliases, version.ref and artifact fields against target BOM')
    text = re.sub(r'(io\.koraframework:[\w-]+:)(?:1\.[0-9A-Za-z.+_-]+|2\.0\.0\.RC1)\b',
                  lambda m: m.group(1) + args.target_version, text)
    names = {args.kora_version_property, 'kora.version'}
    for name in names:
        text = re.sub(r'(?m)^([ \t]*' + re.escape(name) + r'[ \t]*=[ \t]*)(?:1\.[\w.+-]+|2\.0\.0\.RC1)(?=[ \t\r]*(?:#|$))',
                      lambda m: m.group(1) + args.target_version, text)
        text = re.sub(r'(<'+re.escape(name)+r'>)\s*(?:1\.[\w.+-]+|2\.0\.0\.RC1)\s*(</'+re.escape(name)+r'>)',
                      lambda m: m.group(1) + args.target_version + m.group(2), text)
    if 'KspTask' in text:
        reviews.append('Review custom KSP task wiring against the selected KSP API')
    if args.update_tooling:
        for plugin, prop, old, new in (
                ('org.jetbrains.kotlin.jvm', 'kotlinVersion', r'(?:1\.9\.25|2\.4\.10)', args.kotlin_version),
                ('com.google.devtools.ksp', 'kspVersion', r'(?:1\.9\.25-1\.0\.20|2\.3\.11)', args.ksp_version)):
            text = re.sub(r'(id\s*\(?\s*["\']'+re.escape(plugin)+r'["\']\s*\)?\s*version\s*\(?\s*["\'])'+old+r'(["\'])',
                          lambda m: m.group(1)+new+m.group(2), text)
            text = re.sub(r'(?m)^([ \t]*'+prop+r'[ \t]*=[ \t]*)'+old+r'(?=[ \t\r]*(?:#|$))',
                          lambda m: m.group(1)+new, text)
    return text, reviews


def destination(path, root):
    parts = path.relative_to(root).parts
    for i in range(len(parts)-3):
        if parts[i:i+3] == ('ru', 'tinkoff', 'kora'):
            return root.joinpath(*parts[:i], 'io', 'koraframework', *parts[i+3:])
    return path


def main():
    parser = common.parser(__doc__)
    parser.add_argument('--target-version', default='2.0.0.RC2')
    parser.add_argument('--kora-version-property', default='koraVersion')
    parser.add_argument('--move-packages', action='store_true', help='Move Java/Kotlin source package paths after collision preflight')
    parser.add_argument('--update-tooling', action='store_true', help='Update recognized Kotlin/KSP pins; review compatibility first')
    parser.add_argument('--kotlin-version', default='2.4.20')
    parser.add_argument('--ksp-version', default='2.3.12')
    args = parser.parse_args()
    if not re.fullmatch(r'2\.[0-9A-Za-z.+_-]+', args.target_version):
        parser.error('--target-version must be a literal Kora 2.x version')
    root = args.project_root.resolve()
    plans, reviews = [], []
    for path in common.files(args, SUFFIXES):
        old = common.read(path)
        new, notices = migrate_text(path, old, args)
        target = destination(path, root) if args.move_packages and path.suffix in {'.java', '.kt'} else path
        plans.append((path, target, old, new))
        reviews.extend(f'{path.relative_to(root)}: {notice}' for notice in notices)
    return common.execute(args, plans, reviews)


if __name__ == '__main__':
    common.run(main)
