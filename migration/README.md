# Миграция любого проекта Kora 1.x → Kora 2.x

Набор переносится целиком в отдельный каталог и работает с явно выбранным проектом.
Расположение исходников, имена модулей и классов приложения не фиксированы. Поддерживаются
Java, Kotlin, смешанные проекты, Gradle Groovy/Kotlin DSL, Maven и version catalogs.
Автоматизация покрывает ограниченный набор механических изменений; архитектура, удалённые
интеграции и неоднозначные вызовы требуют проверки по языковому руководству.

Текущий проверенный релизный baseline: Kora `2.0.0.RC2`, JDK 25, Kotlin `2.4.20`, KSP `2.3.12`.
Для другой версии 2.x используйте `--target-version`; затем сверьте её BOM/API и генераторы.
Это не обещание совместимости с будущими релизами. Установка локального Kora checkout не требуется.

## Состав

| Файл | Назначение |
|---|---|
| [Java guide](KORA_2_JAVA_MIGRATION_GUIDE.md) | Сборка, API, конфигурация, тесты и native для Java |
| [Kotlin guide](KORA_2_KOTLIN_MIGRATION_GUIDE.md) | Kotlin/KSP, синхронные контракты, nullability и тесты |
| [Инструкции для агентов](KORA_2_MIGRATION_NEURO.md) | Семантические преобразования и диагностические сценарии |
| [OpenRewrite](openrewrite/README.md) | Java AST-рецепты и границы применимости |
| `scripts/migrate_kora_2.py` | Пакеты/FQCN, координаты, literal Kora versions, импортированные типы |
| `scripts/migrate_api_renames.py` | API с проверкой явного Kora import; неоднозначные вызовы → REVIEW |
| `scripts/migrate_config_keys.py` | Ключи внутри известных/заданных путей конфигурации |
| `scripts/migrate_resilient_specs.py` | Типизированные specs из импортированных Kora-аннотаций |
| `scripts/migration_common.py` | Общая инфраструктура CLI; поставляется вместе со скриптами |

## Установка и область запуска

Нужен Python 3.11+. Скопируйте весь каталог набора, сохранив `scripts/migration_common.py` рядом
со скриптами. Целевой проект может находиться в совершенно другом каталоге.

POSIX shell (пути здесь — пользовательские переменные):

```shell
export MIGRATION_TOOLS=/absolute/path/to/migration-toolkit
export PROJECT_ROOT=/absolute/path/to/your-project
python "$MIGRATION_TOOLS/scripts/migrate_kora_2.py" --project-root "$PROJECT_ROOT" --diff
```

PowerShell:

```powershell
$tools = 'C:\tools\kora-migration'
$project = 'C:\work\your-service'
python "$tools\scripts\migrate_kora_2.py" --project-root "$project" --diff
```

Общие параметры **всех четырёх** команд:

| Параметр | Семантика |
|---|---|
| `--project-root PATH` | Корень целевого проекта; по умолчанию текущий рабочий каталог, а не каталог скрипта |
| `[PATH ...]` | Файлы/каталоги относительно корня; без них весь проект, включая root build files |
| `--exclude GLOB` | Повторяемые исключения относительно корня, например `vendor/*` или `**/generated` |
| `--diff` | Unified diff для проверки |
| `--apply` | Запись плана; без параметра только dry-run |

Несуществующие и выходящие за корень пути приводят к ошибке до записи. Перекрывающиеся пути
обрабатываются один раз. Symlink/junction/reparse points не обходятся. Каталоги `.git`, `.gradle`,
`.idea`, `.kotlin`, `build`, `target`, `out`, `node_modules`, `__pycache__`, `.venv`, `venv`
исключены; сам каталог инструментария также исключён, если скопирован внутрь проекта.
Другие generated/vendor каталоги исключайте явно. Обрабатываются UTF-8/UTF-8 BOM;
исходные переводы строк сохраняются. Не-UTF-8 файл вызывает ошибку до применения плана.

## Порядок миграции

1. Сохраните исходное состояние проекта в VCS, зафиксируйте проходящую 1.x сборку и список интеграций.
2. Выберите точную целевую версию, репозитории артефактов, JDK и совместимые Kotlin/KSP.
3. Выполните dry-run каждого применимого этапа с `--diff`, проверьте diff и строки `REVIEW`.
4. Повторите проверенные команды с `--apply`. Скрипты независимы; отдельного RC-этапа нет.
5. Перегенерируйте AP/KSP/OpenAPI/protobuf, соберите production/test source sets без старого build cache.
6. Проверьте runtime config, HTTP, transactions, messaging, metrics/traces, integration/native сценарии.

```shell
python "$MIGRATION_TOOLS/scripts/migrate_kora_2.py" --project-root "$PROJECT_ROOT" --target-version 2.0.0.RC2 --diff
python "$MIGRATION_TOOLS/scripts/migrate_api_renames.py" --project-root "$PROJECT_ROOT" --diff
python "$MIGRATION_TOOLS/scripts/migrate_config_keys.py" --project-root "$PROJECT_ROOT" --diff
python "$MIGRATION_TOOLS/scripts/migrate_resilient_specs.py" --project-root "$PROJECT_ROOT" --diff
```

Для отдельного модуля: добавьте `services/orders` после параметров. Root `pom.xml`, `build.gradle*`,
`gradle.properties`, `buildSrc`, included builds и version catalog вне этой области автоматически
не меняются — включите нужные файлы дополнительными аргументами либо запустите из корня без PATH.

## Настройка механического этапа

`migrate_kora_2.py`:

- `--target-version 2.0.0.RC2`: literal версия Kora; меняет известные 1.x/RC1 pins и координаты.
- `--kora-version-property frameworkVersion`: другое имя свойства вместо `koraVersion`;
  Maven `<kora.version>` поддерживается дополнительно. SNAPSHOT и уже выбранные новые версии сохраняются.
- `--update-tooling`: отдельное согласованное обновление распознаваемых Kotlin/KSP pins.
  `--kotlin-version` и `--ksp-version` задают целевые значения; default `2.4.20` / `2.3.12`.
  Эти параметры не переписывают произвольные version catalog aliases или convention plugins.
- `--move-packages`: дополнительно переносит Java/Kotlin файлы с сегментами пути `ru/tinkoff/kora`
  в `io/koraframework`. Обычные пакеты приложения `com.acme.*` не меняются. Не требует стандартного `src` layout.
  Сначала проверяются все коллизии; пустые старые каталоги могут остаться.

Пакеты в исходниках меняются вне comments/string literals. В конфигурации/build/resources
меняются точные Kora namespace/FQCN и координаты, включая строковые ссылки на encoder.
Custom FQCN в Java/Kotlin строках и reflection registries проверьте вручную.
Типы с изменённым simple name переименовываются только при явном import; wildcard/alias/import shadowing
и любые спорные случаи проверяются через AST/компиляцию. Скрипт не является type resolver.

JUnit, MockK, GraalVM, OpenAPI plugin, Java toolchain, KAPT сторонних процессоров, произвольные
`parameters`, аннотации nullability и `suspend` глобально не переписываются. Их выбор и изменения
приведены в guides; OpenRewrite предлагает некоторые явно включаемые дополнительные рецепты.

## Конфигурация и custom factory paths

`migrate_config_keys.py` по умолчанию знает `httpServer` и `openapi.management`.
Если фабрика использует другие пути, передайте `--http-server-path api` / `--openapi-path apiDocs`.
Несвязанные `file`, `rapidoc`, `slidingWindowSize`, `db` и `publicApiHttpPort` остаются без изменений.

```shell
python "$MIGRATION_TOOLS/scripts/migrate_config_keys.py" --project-root "$PROJECT_ROOT" \
  --http-server-path services.api --openapi-path docs.management \
  --circuit-breaker-path resilient.circuitbreaker.payments --diff
```

Circuit-breaker path повторяется для каждого подтверждённого блока; размер переводится в
`FIXED_WINDOW` + `countBased.windowSize`, если нет конфликтующей конфигурации.
YAML получает настоящий вложенный `system`, HOCON/properties — dotted keys.
Простая строка OpenAPI `file` превращается в список `files`. Inline objects, YAML anchors/aliases,
многосложный HOCON, include/substitution resolution, source-embedded конфиг и conflicts переносите
вручную либо schema-aware инструментом. Скрипт не интерпретирует runtime config.

`db` → `jdbc` **не является универсальным правилом**: путь определяет выбранная factory.
Сверьте factory constructor и `@ConfigSource`, измените путь или фабрику согласованно.

## Resilient specs и границы автоматизации

Генерация требует явного Kora import. Имена specs выводятся из строковых имён аннотаций, а не
из имён примеров; код генерируется рядом с исходником в его package. Стандартный путь
`resilient.<kind>.<name>` нужно проверить на своей конфигурации. `--config-prefix company.resilience`
меняет корень generated paths, `--spec-prefix Migration` добавляет префикс имён типов.
Индивидуальный custom path исправьте в spec после генерации.
Неизвестные imports/выражения/aliases остаются на review. Существующий spec переиспользуется только
при полном совпадении; коллизии нормализованных имён и несовместимые существующие файлы останавливают
весь этап до записи. Переименуйте spec/вынесите общий интерфейс вручную и повторите dry-run.

Перед записью каждый этап собирает полный план и проверяет создаваемые/переносимые файлы.
Это preflight, а не файловая транзакция: сбой диска во время записи требует восстановления из VCS.
`REVIEW` не означает успешной полной миграции. Идемпотентность означает отсутствие повторных
механических правок; runtime-корректность подтверждается тестами приложения.

## Проверки набора

```shell
python -B -m unittest discover -s "$MIGRATION_TOOLS/scripts" -p 'test_*.py'
gradle -p "$MIGRATION_TOOLS/openrewrite" test
```

Для OpenRewrite нужен совместимый установленный Gradle и JDK 21 parser toolchain; мигрируемое
приложение использует свой wrapper/build и JDK целевого релиза. Python-тесты создают отдельные
проекты вне каталога инструментария и проверяют CLI, scope, dry-run, переносимость, коллизии,
повторный запуск и сохранность несвязанного кода. Это не интеграционная сборка любого проекта.
