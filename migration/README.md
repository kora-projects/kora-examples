# Kora 2 migration automation

- `openrewrite/rewrite.yml`: AST-safe Java/Gradle recipes.
- `openrewrite/build.gradle`: standalone Rewrite runner configuration.
- `scripts/migrate_kora_2.py`: deterministic dry-run/apply fallback for Gradle Kotlin DSL, Groovy DSL, OpenAPI options, resources, and package-directory moves.

Run scripts from repository root. Use dry-run first:

```shell
python migration/scripts/migrate_kora_2.py
python migration/scripts/migrate_kora_2.py --apply
```
