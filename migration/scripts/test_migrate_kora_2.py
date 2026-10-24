"""Portable CLI regression tests; fixtures are independent target projects."""
from pathlib import Path
import os
import shutil
import subprocess
import sys
import tempfile
import unittest

TOOLS = Path(__file__).parent


class PortableMigrationTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.base = Path(self.tmp.name)
        self.root = self.base / 'arbitrary-service'
        self.root.mkdir()
        self.tools = self.base / 'toolkit' / 'scripts'
        self.tools.mkdir(parents=True)
        for path in TOOLS.glob('*.py'):
            if not path.name.startswith('test_'):
                shutil.copy2(path, self.tools / path.name)

    def put(self, relative, text):
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(text.encode('utf-8'))
        return path

    def run_tool(self, tool='migrate_kora_2', *args, expected=0, cwd=None):
        env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
        result = subprocess.run([sys.executable, '-B', str(self.tools / (tool+'.py')), *args],
                                cwd=cwd or self.root, text=True, capture_output=True, env=env)
        self.assertEqual(result.returncode, expected, result.stdout+result.stderr)
        return result.stdout+result.stderr

    def test_default_cwd_dry_run_apply_idempotence(self):
        p = self.put('service/src/main/java/com/acme/App.java',
                     'import ru.tinkoff.kora.common.KoraApp;\r\n@KoraApp interface App {}\r\n')
        before = p.read_bytes()
        self.assertIn('CHANGE', self.run_tool())
        self.assertEqual(p.read_bytes(), before)
        self.run_tool('migrate_kora_2', '--apply')
        self.assertIn(b'io.koraframework.common.annotation.KoraApp;\r\n', p.read_bytes())
        self.assertIn('0 file(s)', self.run_tool())

    def test_explicit_root_scope_and_exclusion(self):
        a = self.put('one/src/App.kt', 'import ru.tinkoff.kora.common.Component\n@Component class App')
        b = self.put('two/src/App.kt', a.read_text())
        skip = self.put('one/src/generated/Old.kt', a.read_text())
        self.run_tool('migrate_kora_2', '--project-root', str(self.root), '--exclude', '**/generated', '--apply', 'one', cwd=self.base)
        self.assertIn('io.koraframework', a.read_text())
        self.assertIn('ru.tinkoff', b.read_text())
        self.assertIn('ru.tinkoff', skip.read_text())

    def test_build_outputs_and_unrelated_source(self):
        source = 'class Other { String parameters = "ru.tinkoff.kora.common.Component";\n// ru.tinkoff.kora.common.Component\n@Nullable String data; void readUnchecked() {} }'
        p = self.put('src/main/java/Other.java', source)
        generated = self.put('build/generated/Old.java', 'ru.tinkoff.kora.common.Component')
        self.run_tool('migrate_kora_2', '--apply')
        self.assertEqual(p.read_text(), source)
        self.assertIn('ru.tinkoff', generated.read_text())

    def test_build_target_versions_and_tooling_opt_in(self):
        p = self.put('build.gradle.kts', 'id("org.jetbrains.kotlin.jvm") version "1.9.25"\nimplementation("ru.tinkoff.kora:kora-parent:1.2.0")\n// kapt belongs to another processor\nkapt("org.mapstruct:mapstruct-processor:1.6.3")\n')
        props = self.put('gradle.properties', 'frameworkVersion=1.2.0\nkoraVersion=2.0.0-SNAPSHOT\n')
        self.run_tool('migrate_kora_2', '--apply', '--target-version', '2.1.0', '--kora-version-property', 'frameworkVersion')
        self.assertIn('io.koraframework:kora-bom:2.1.0', p.read_text())
        self.assertIn('version "1.9.25"', p.read_text())
        self.assertIn('kapt("org.mapstruct', p.read_text())
        self.assertIn('frameworkVersion=2.1.0', props.read_text())
        self.assertIn('koraVersion=2.0.0-SNAPSHOT', props.read_text())
        self.run_tool('migrate_kora_2', '--apply', '--update-tooling')
        self.assertIn('version "2.4.20"', p.read_text())

    def test_maven_and_catalog(self):
        p = self.put('pom.xml', '<project><dependencies><dependency><groupId>ru.tinkoff.kora</groupId><artifactId>kora-parent</artifactId><version>1.2.0</version></dependency><dependency><groupId>com.acme</groupId><artifactId>kora-parent</artifactId><version>1.2.0</version></dependency></dependencies></project>')
        catalog = self.put('gradle/libs.versions.toml', '[libraries]\nkora = { module = "ru.tinkoff.kora:json-module:1.2.0" }\n')
        self.run_tool('migrate_kora_2', '--apply')
        self.assertIn('<artifactId>kora-bom</artifactId><version>2.0.0.RC2</version>', p.read_text())
        self.assertIn('<groupId>com.acme</groupId><artifactId>kora-parent</artifactId><version>1.2.0', p.read_text())
        self.assertIn('io.koraframework:json-common:2.0.0.RC2', catalog.read_text())

    def test_package_collision_preflight(self):
        p = self.put('src/ru/tinkoff/kora/custom/App.java', 'package ru.tinkoff.kora.custom; class App {}')
        self.put('src/io/koraframework/custom/App.java', 'unrelated')
        self.run_tool('migrate_kora_2', '--apply', '--move-packages', expected=1)
        self.assertIn('ru.tinkoff.kora', p.read_text())

    def test_package_move(self):
        p = self.put('src/main/java/ru/tinkoff/kora/custom/App.java', 'package ru.tinkoff.kora.custom; class App {}')
        self.run_tool('migrate_kora_2', '--apply', '--move-packages')
        self.assertFalse(p.exists())
        self.assertTrue((self.root/'src/main/java/io/koraframework/custom/App.java').exists())

    def test_missing_and_outside_paths(self):
        self.assertIn('does not exist', self.run_tool('migrate_kora_2', 'missing', expected=1))
        self.assertIn('escapes project root', self.run_tool('migrate_kora_2', str(self.base), expected=1))

    def test_api_preserves_unowned_methods_and_annotations(self):
        p = self.put('src/Other.java', 'class Other { void readUnchecked() {} Object extract() {} @Fallback(value = "x", method = "f()") void f() {} }')
        before = p.read_text()
        self.assertIn('REVIEW', self.run_tool('migrate_api_renames', '--apply'))
        self.assertEqual(p.read_text(), before)

    def test_api_owned_fallback_and_comments(self):
        p = self.put('src/Service.java', 'import ru.tinkoff.kora.resilient.fallback.annotation.Fallback;\nclass Service { @Fallback(value = "x", method = "f()") void f() {}\n// @Fallback(value = "x", method = "f()")\n}')
        self.run_tool('migrate_api_renames', '--apply')
        self.assertIn('@Fallback(method = "f()")', p.read_text())
        self.assertIn('// @Fallback(value = "x", method = "f()")', p.read_text())
        self.assertIn('0 file(s)', self.run_tool('migrate_api_renames'))

    def test_config_hocon_ownership_and_custom_paths(self):
        p = self.put('src/main/resources/application.conf', 'custom {\n  publicApiHttpPort = 1\n  file = "data"\n}\napi {\n  publicApiHttpPort = 8080\n  privateApiHttpPort = 8085\n}\ndb {\n  jdbcUrl = "jdbc:test"\n}\n')
        self.run_tool('migrate_config_keys', '--apply', '--http-server-path', 'api')
        self.assertIn('publicApiHttpPort = 1', p.read_text())
        self.assertIn('  port = 8080', p.read_text())
        self.assertIn('system.port = 8085', p.read_text())
        self.assertIn('db {', p.read_text())
        self.assertIn('0 file(s)', self.run_tool('migrate_config_keys', '--http-server-path', 'api'))

    def test_yaml_system_and_unsupported_anchor(self):
        p = self.put('app.yaml', 'httpServer:\n  privateApiHttpPort: 8085\n  privateApiHttpMetricsPath: /metrics\n  publicApiHttpPort: 8080\nother:\n  publicApiHttpPort: 2\n')
        self.run_tool('migrate_config_keys', '--apply')
        self.assertIn('  system:\n    port: 8085\n    metricsPath: /metrics\n  port: 8080', p.read_text())
        self.assertIn('  publicApiHttpPort: 2', p.read_text())
        anchor = self.put('anchor.yml', 'httpServer: &defaults\n  publicApiHttpPort: 8080\nother: *defaults\n')
        before = anchor.read_bytes()
        self.run_tool('migrate_config_keys', '--apply')
        self.assertEqual(anchor.read_bytes(), before)

    def test_properties_openapi_and_circuit_breaker(self):
        p = self.put('application.properties', 'httpServer.privateApiHttpPort=8085\nopenapi.management.file="spec.yaml"\nresilient.circuitbreaker.api.slidingWindowSize=50\nother.slidingWindowSize=2\n')
        self.run_tool('migrate_config_keys', '--apply', '--circuit-breaker-path', 'resilient.circuitbreaker.api')
        self.assertIn('httpServer.system.port=8085', p.read_text())
        self.assertIn('openapi.management.files = ["spec.yaml"]', p.read_text())
        self.assertIn('resilient.circuitbreaker.api.countBased.windowSize = 50', p.read_text())
        self.assertIn('other.slidingWindowSize=2', p.read_text())

    def test_resilient_java_and_kotlin(self):
        for ext in ('java','kt'):
            with self.subTest(ext=ext):
                p = self.put('module-'+ext+'/Service.'+ext, 'package com.acme;\nimport ru.tinkoff.kora.resilient.retry.annotation.Retry;\nclass Service { @Retry("remote-api") void run() {} }')
                self.run_tool('migrate_resilient_specs', '--apply')
                self.assertIn('@Retryable(RemoteApiRetry'+('::class' if ext=='kt' else '.class')+')', p.read_text())
                generated = p.parent/('RemoteApiRetry.'+ext)
                self.assertIn('resilient.retry.remote-api', generated.read_text())
        self.assertIn('0 file(s)', self.run_tool('migrate_resilient_specs'))

    def test_resilient_collision_no_partial_write(self):
        p = self.put('src/Service.java', 'package com.acme;\nimport io.koraframework.resilient.retry.annotation.Retry;\nclass Service { @Retry("a-b") void run() {} @Retry("a.b") void other() {} }')
        before = p.read_bytes()
        self.run_tool('migrate_resilient_specs', '--apply', expected=1)
        self.assertEqual(before, p.read_bytes())
        self.assertFalse((p.parent/'ABRetry.java').exists())

    def test_existing_spec_collision_no_partial_write(self):
        p = self.put('src/Service.java', 'package com.acme;\nimport io.koraframework.resilient.retry.annotation.Retry;\nclass Service { @Retry("api") void run() {} }')
        self.put('src/ApiRetry.java', 'unrelated class')
        before = p.read_bytes()
        self.run_tool('migrate_resilient_specs', '--apply', expected=1)
        self.assertEqual(before, p.read_bytes())

    def test_non_kora_resilient_preserved(self):
        p = self.put('Service.java', 'package com.acme;\nimport com.acme.Retry;\nclass Service { @Retry("api") void run() {} }')
        before = p.read_bytes()
        self.run_tool('migrate_resilient_specs', '--apply')
        self.assertEqual(before, p.read_bytes())

    def test_bom_and_nested_kotlin_comments(self):
        original = ('\ufeffimport ru.tinkoff.kora.json.common.JsonCommonModule\r\n'
                    '/* outer /* inner */ ru.tinkoff.kora.common.Component */\r\n'
                    'class App : JsonCommonModule\r\n')
        p = self.put('App.kt', original)
        self.run_tool('migrate_kora_2', '--apply')
        data = p.read_bytes().decode('utf-8')
        self.assertTrue(data.startswith('\ufeffimport io.koraframework.json.common.JsonModule'))
        self.assertIn('/* outer /* inner */ ru.tinkoff.kora.common.Component */', data)
        self.assertIn('class App : JsonModule\r\n', data)

    def test_symlink_skipped(self):
        outside = self.base / 'outside'
        outside.mkdir()
        p = outside / 'Secret.java'
        p.write_text('ru.tinkoff.kora.common.Component')
        try:
            (self.root / 'linked').symlink_to(outside, target_is_directory=True)
        except OSError:
            self.skipTest('Creating symlinks is unavailable on this host')
        self.run_tool('migrate_kora_2', '--apply')
        self.assertEqual(p.read_text(), 'ru.tinkoff.kora.common.Component')

    def test_existing_system_is_not_overwritten(self):
        source = ('httpServer:\n  system:\n    port: 9000\n'
                  '  privateApiHttpPort: 8085\n')
        p = self.put('app.yaml', source)
        output = self.run_tool('migrate_config_keys', '--apply')
        self.assertIn('REVIEW', output)
        self.assertEqual(p.read_text(), source)

    def test_hocon_closing_comment_does_not_leak_scope(self):
        p = self.put('app.conf', 'httpServer {\n  publicApiHttpPort = 8080\n} // end\nother {\n  publicApiHttpPort = 99\n}\n')
        self.run_tool('migrate_config_keys', '--apply')
        self.assertIn('  port = 8080', p.read_text())
        self.assertIn('  publicApiHttpPort = 99', p.read_text())

    def test_custom_spec_prefix_and_path(self):
        p = self.put('src/Service.java', 'package com.acme;\nimport io.koraframework.resilient.retry.annotation.Retry;\nclass Service { @Retry("api") void run() {} }')
        self.run_tool('migrate_resilient_specs', '--apply', '--spec-prefix', 'Remote', '--config-prefix', 'company.resilience')
        self.assertIn('RemoteApiRetry.class', p.read_text())
        self.assertIn('company.resilience.retry.api', (p.parent/'RemoteApiRetry.java').read_text())

    def test_toolkit_inside_target_is_excluded(self):
        nested = self.root / 'migration'
        shutil.copytree(self.tools.parent, nested)
        own = nested / 'openrewrite/src/test/Old.java'
        own.parent.mkdir(parents=True)
        own.write_text('ru.tinkoff.kora.common.Component')
        env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
        result = subprocess.run([sys.executable, '-B', str(nested/'scripts/migrate_kora_2.py'), '--apply'], cwd=self.root, capture_output=True, text=True, env=env)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(own.read_text(), 'ru.tinkoff.kora.common.Component')


if __name__ == '__main__':
    unittest.main()
