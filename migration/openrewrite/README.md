# OpenRewrite-рецепты Kora 1.x → 2.0

Сам рецепт — `src/main/resources/META-INF/rewrite/kora-1-to-2.yml`. Каноническое расположение:
модуль собирается в jar, который можно положить в конфигурацию `rewrite` любого проекта,
либо файл можно указать напрямую через `configFile`.

## Состав

| Рецепт | Что делает |
|---|---|
| `io.koraframework.migration.RelocateKoraPackages` | `ru.tinkoff.kora.*` → `io.koraframework.*` |
| `io.koraframework.migration.RelocateKoraAnnotations` | DI-аннотации из `common` в `common.annotation` |
| `io.koraframework.migration.RenameKoraTypes` | типы, сменившие имя или модуль |
| `io.koraframework.migration.MigrateNullabilityAnnotations` | `jakarta.annotation.Nullable` → JSpecify |
| `io.koraframework.migration.UpdateKoraDependencies` | координаты сборки |
| `io.koraframework.migration.NormalizeKotlinDependencies` | прямые `implementation(platform(...))`, versioned `ksp`, удаление лишнего `kspTest` и устаревшего processor wiring |
| `io.koraframework.migration.Kora1To2` | агрегат всех шести |

Каждый применим отдельно — например, если координаты в проекте уже поменяны руками.

## Как применить к своему проекту

В корневой build-файл **мигрируемого** проекта:

```groovy
plugins {
    id "org.openrewrite.rewrite" version "7.19.0"
}

dependencies {
    rewrite "org.openrewrite.recipe:rewrite-java-dependencies:1.36.0"
}

rewrite {
    activeRecipe("io.koraframework.migration.Kora1To2")
    configFile = file("<путь>/migration/openrewrite/src/main/resources/META-INF/rewrite/kora-1-to-2.yml")
}
```

```shell
./gradlew rewriteDryRun   # патч в build/reports/rewrite/rewrite.patch
./gradlew rewriteRun
```

Запускать `rewriteRun` внутри самого `migration/openrewrite` бессмысленно: это отдельный Gradle-build
со своим `settings.gradle`, и видит он только свои исходники, а не репозиторий примеров.

## Тесты

```shell
cd migration/openrewrite && ../../gradlew test
```

`Kora1To2Test` — before/after на каждую группу Java-правил плюс проверка идемпотентности
(на уже мигрированном коде рецепт не делает ничего). На момент последнего прогона — 5/5.

## Чего тесты НЕ покрывают и почему

`UpdateKoraDependencies` построен на `org.openrewrite.java.dependencies.ChangeDependency`, у которого
два ограничения, делающих модульный тест невозможным:

1. **На Gradle-файлах он работает только при наличии маркера `GradleProject`**, который ставит
   Gradle-плагин во время реального запуска. В `RewriteTest` такого маркера нет, и рецепт молча
   не делает ничего (проверено отдельным пробником). Официальный обход — `withToolingApi()` из
   `rewrite-gradle-tooling-model`, но этот артефакт не опубликован для ветки 8.56.x.
2. **Переименование artifactId применяется только после успешного резолва новых координат.**
   Пары вида `ru.tinkoff.kora:cache-redis:1.1.29` → `io.koraframework:cache-redis-lettuce:1.1.29`
   не резолвятся никогда: версии 1.x под новой группой не существует по определению,
   а `cache-redis-lettuce` есть только в снапшоте 2.0 (на Maven Central лежит `2.0.0.alpha6`,
   где этого артефакта ещё нет). Поэтому перед запуском рецепта версию Kora надо поднять
   до 2.0 и иметь соответствующий репозиторий (Maven Local после `publishToMavenLocal` либо
   snapshots-репозиторий Sonatype).

Сам репозиторий `kora-examples` мигрировался **скриптом** `migration/scripts/migrate_kora_2.py`,
а не этим рецептом: скрипт меняет и версию, и координаты одним проходом и не требует,
чтобы промежуточное состояние резолвилось. Рецепт здесь — для чужих проектов, где нужна
именно AST-безопасная правка Java, а не текстовые замены.

## Что рецепт не делает вообще

- **Kotlin не трогает.** OpenRewrite парсит Kotlin экспериментально; для Kotlin-модулей
  рабочая автоматика — скрипт.
- **Не ставит JSpecify в type-use позицию.** Меняется только тип аннотации; правильное место
  для дженериков и массивов — семантика, описана в языковых руководствах.
- **Не трогает каталоги ресурсов**, включая `META-INF/native-image/<group>/` — их переименовывают
  вручную.
- **Не делает семантику**: resilient-спецификации, снятие `suspend`, S3-клиент, сгенерированный
  OpenAPI-код. Это `KORA_MIGRATION_NEURO.md`.
