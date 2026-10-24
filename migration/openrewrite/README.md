# OpenRewrite: Kora 1.x → Kora 2.x для любого Java-проекта

Рецепты находятся в `src/main/resources/META-INF/rewrite/kora-1-to-2.yml` относительно этого
каталога. Они подключаются к **целевому проекту**, не требуют его расположения рядом с инструментарием.
Python-скрипты и OpenRewrite — альтернативные/дополняющие способы обработки пересекающихся правил;
проверяйте dry-run каждого этапа, не применяйте слепо оба набора.

## Основной набор

`io.koraframework.migration.Kora1To2` включает:

| Рецепт | Область |
|---|---|
| `RelocateKoraPackages` | Java package/type references `ru.tinkoff.kora` → `io.koraframework` |
| `RelocateKoraAnnotations` | Перенос известных DI-аннотаций в `common.annotation` |
| `RenameKoraTypes` | Известные типы модулей и Logback encoder; XML/JSON FQCN энкодера |
| `MigrateNullabilityAnnotations` | Attributed Java `jakarta.annotation.Nullable` → JSpecify; placement требует проверки |
| `UpdateKoraDependencies` | Kora group/artifact moves; conventional version properties |
| `NormalizeKotlinDependencies` | Точные текстовые формы BOM/KSP dependencies в Gradle Kotlin DSL |

Все имена имеют префикс `io.koraframework.migration.`. Java type recipes используют attribution;
они не разрешают runtime config и receiver types в неподдержанных исходниках. Kotlin dependency
recipe работает с build text, а не с Kotlin AST. Custom build blocks и KAPT wiring сохраняются.

Текущий baseline YAML — Kora `2.0.0.RC2`, Kotlin `2.4.20`, KSP `2.3.12`.
Если выбрана другая версия 2.x, сначала обновите versions/coordinates через
`migrate_kora_2.py --project-root <project> --target-version <version>` либо вручную.
Property rules не перезаписывают уже выбранную другую новую версию или SNAPSHOT.
При полностью самостоятельном использовании YAML измените target version rules в копии рецепта;
рецепт не имеет runtime-параметра `--target-version`. Custom property/alias names остаются ручной работой.

## Дополнительные рецепты включаются явно

Следующие рецепты **не входят** в основной агрегат:

- `UpdateJUnit`: literal JUnit pins → `6.1.3`.
- `UpdateMockK`: literal MockK pins → `1.14.9`.
- `RemoveEmptyEnvironmentBlock`: удаляет точный пустой Gradle environment placeholder.
- `RemoveSuspendHttpClientMethods`, `RemoveSuspendRepositoryMethods`, `RemoveSuspendHttpServerMethods`:
  эвристические текстовые правки только по conventional filename patterns.

Последние три не определяют принадлежность API к Kora. Используйте их только после проверки
каждого затронутого файла; custom filenames ими не покрываются. В общем случае меняйте Kotlin
контракты и coroutine call chains по языковому guide. Удаление сторонних processors, nullability,
плагинов и версии JDK не выполняется по имени файла.

## Подключение из произвольного проекта

Выберите совместимую комбинацию OpenRewrite plugin/recipe dependencies по сборке проекта.
Ниже — пример Gradle Groovy DSL с absolute path к YAML:

```groovy
plugins {
    id "org.openrewrite.rewrite" version "7.19.0"
}

dependencies {
    rewrite "org.openrewrite.recipe:rewrite-java-dependencies:1.36.0"
}

rewrite {
    activeRecipe("io.koraframework.migration.Kora1To2")
    configFile = file("/absolute/path/to/toolkit/openrewrite/src/main/resources/META-INF/rewrite/kora-1-to-2.yml")
}
```

Для Gradle Kotlin DSL используйте эквивалентные `id(...)`, `rewrite(...)`, `activeRecipe(...)`.
Windows path задайте с `/` или корректно экранируйте `\`. Сам инструментарий не должен быть
subproject приложения: `openrewrite/settings.gradle` описывает отдельную сборку рецептов.
Выполняйте wrapper **своего приложения** из его корня:

```shell
./gradlew rewriteDryRun
# Просмотреть build/reports/rewrite/rewrite.patch.
./gradlew rewriteRun
```

Для multi-module и composite builds подключите плагин/конфигурацию к нужным builds и убедитесь,
что dry-run видит каждый source set. Maven: подключите декларативный YAML через rewrite-maven-plugin
вашей сборки либо упакуйте ресурсы рецепта в jar и укажите его dependency; проверьте dry-run
по всем reactor modules. Gradle tooling markers неприменимы к Maven-модели.

Jar с рецептом можно собрать отдельно и добавить в recipe classpath целевого проекта.
Artifact coordinates такого jar определяет ваша публикация; они не заданы как внешний обязательный сервис.

## Dependency resolution и границы покрытия

`ChangeDependency` требует build model. В Gradle он получает её от плагина, поэтому простой
unit test на строке build.gradle не проверяет реальное изменение зависимостей.
Новые coordinates должны разрешаться в выбранном repository. Нельзя ожидать, что
`io.koraframework:<artifact>:1.x` существует после одного изменения groupId.
Обновляйте group, artifact и version согласованно: Python stage делает это за проход для
распознанных literal форм; BOM, catalogs, convention plugins и `${...}` properties проверьте отдельно.

Java AST tests не подтверждают работу в каждой версии Gradle/Maven и не заменяют dry-run/build
реального проекта. Скрипты не выбирают произвольный runtime transport, config path или
замену удалённой интеграции. Native resources, Kotlin source semantics, OpenAPI generated code,
custom factories и type-use nullability покрываются руководствами и тестами приложения.

## Проверка рецептов

С совместимым установленным Gradle:

```shell
gradle -p /absolute/path/to/toolkit/openrewrite test
```

Тестовый проект использует JDK 21 parser toolchain; целевая Kora-сборка должна использовать
JDK своего релиза (baseline JDK 25). Для конкретной модификации запускайте только необходимые
проверки по согласованию с владельцем проекта. Отчёты текущего запуска являются источником
результата; здесь не фиксируется число тестов как обещание проверки чужого приложения.
