# Миграция Kora 1.x → 2.0

## Состав

| Файл | Назначение |
|---|---|
| `KORA_2_JAVA_MIGRATION_GUIDE.md` | самодостаточное руководство для Java-проектов |
| `KORA_2_KOTLIN_MIGRATION_GUIDE.md` | самодостаточное руководство для Kotlin-проектов |
| `KORA_2_MIGRATION_STATUS.md` | статус каждого Gradle-модуля репозитория, по данным реальных прогонов сборки |
| `KORA_2_FRAMEWORK_ISSUES.md` | журнал дефектов и гипотез по самому фреймворку |
| `KORA_MIGRATION_NEURO.md` | инструкции для ИИ-агентов на недетерминированные трансформации |
| `openrewrite/` | AST-безопасные рецепты для Java и Gradle |
| `scripts/` | детерминированные текстовые/файловые преобразования |

Предыдущий сводный документ `KORA_2_MIGRATION_GUIDE.md` разложен на два языковых руководства без потери содержания;
ссылки на несуществующий снапшот `agents-md/kora-2` заменены на реальные источники: локальный чекаут фреймворка `../kora` @ `master`
и документацию 1.x в `agents-md/kora-docs`.

## Порядок запуска

```shell
# 1. Java + Gradle, AST-безопасно
cd migration/openrewrite && ../../gradlew rewriteRun

# 2. Остальное: Kotlin, Gradle DSL, ресурсы, перенос каталогов пакетов
python migration/scripts/migrate_kora_2.py            # dry-run, печатает план
python migration/scripts/migrate_kora_2.py --apply    # применить

# 3. Первая сборка после смены пакетов — обязательно так
export JAVA_HOME=<JDK 25>
./gradlew clean --continue
./gradlew classes testClasses --continue --no-build-cache
```

Оба слоя автоматизации идемпотентны. Перед запуском сохраните рабочее дерево.

## Требования к окружению

- **JDK 25 для самого Gradle-процесса**, не только в toolchain: `io.koraframework:openapi-generator` попадает в buildscript classpath и требует JVM 25.
- Локально опубликованный фреймворк: в `../kora` выполнить `./gradlew publishToMavenLocal` (версия `2.0.0-SNAPSHOT`), в примерах — `koraVersion=2.0.0-SNAPSHOT`.
- Docker — для интеграционных тестов на Testcontainers.
- GraalVM `native-image` — для native-сборок; при его отсутствии GraalVM-модули мигрируются только в JVM-части.

## Известные ограничения автоматизации

`scripts/migrate_kora_2.py` писался под этот репозиторий и **пока не пригоден к переносу в чужой проект без доработки**:

- содержит замены с хардкодом имён классов конкретных примеров (`SimpleCache`, `CompositeCache`, `UserContextMapping`);
- применяет широкие текстовые замены ко всем типам файлов, включая `.conf`/`.yaml`/`.xml` (например, `parameters =` → `args =`), что вне Kora-кода может задеть постороннее;
- удаляет строки `@Nonnull` / `@NonNull` и `@Nullable` без анализа позиции аннотации;
- не имеет режима ограничения области применения (каталог/модуль).

OpenRewrite-рецепт **не трансформирует Kotlin** — для Kotlin-модулей рабочей автоматикой остаётся скрипт.

Что автоматизация принципиально не делает: типизированные resilient-спецификации, снятие `suspend` по цепочке вызовов,
корректная расстановка type-use аннотаций JSpecify, адаптация сгенерированного OpenAPI-кода, миграция S3-клиента.
Эти классы изменений описаны в `KORA_MIGRATION_NEURO.md`.
