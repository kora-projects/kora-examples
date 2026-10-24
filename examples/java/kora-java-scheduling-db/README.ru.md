# Kora Java DB Scheduling

Пример модуля DB Scheduling в Kora с хранением расписания в PostgreSQL.

В примере использовались модули:
- [Scheduling](https://kora-projects.github.io/kora-docs/ru/documentation/scheduling/)
- [Flyway](https://kora-projects.github.io/kora-docs/ru/documentation/database-migration/)
- [JDBC Database](https://kora-projects.github.io/kora-docs/ru/documentation/database-jdbc/)
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)

## Build

Собрать классы:

```shell
./gradlew classes
```

Собрать артефакт:

```shell
./gradlew distTar
```

## Run

Запустить PostgreSQL из каталога примера:

```shell
docker compose up -d --wait
```

Запустить локально:

```shell
./gradlew run
```

Flyway выполняет миграции из `src/main/resources/db/migration` при инициализации JDBC
перед запуском scheduler. `V1` создаёт таблицу scheduler, `V2` — таблицу бизнес-событий.
`scheduling.dbScheduler.tableInitialize` выключен: схемой управляет Flyway.
Изменения схемы добавляйте новыми версионными миграциями; применённые SQL-файлы не редактируйте.

Если база уже создана старым Docker init script без Flyway history, перед переходом
сверьте схему и выполните контролируемый baseline до версии 2. Для пустой базы baseline не нужен.

`POSTGRES_JDBC_URL` задаёт адрес БД, `HTTP_PORT` и `SYSTEM_PORT` — порты приложения.
Конфигурация находится в `src/main/resources/application.conf`.

Для двух экземпляров соберите дистрибутив:

```shell
./gradlew installDist
```

В отдельных терминалах из каталога примера (POSIX shell):

```shell
HTTP_PORT=8080 SYSTEM_PORT=8085 build/install/application/bin/application
HTTP_PORT=8081 SYSTEM_PORT=8086 build/install/application/bin/application
```

В PowerShell задайте `$env:HTTP_PORT` и `$env:SYSTEM_PORT` в каждом терминале,
затем выполните `build/install/application/bin/application.bat`.

Оба экземпляра используют общую БД и стабильное имя задачи `example-tick`.
`GET /ticks` возвращает число записанных бизнес-событий. При остановке одного экземпляра
второй продолжает выполнять задачу. Это persistent scheduling: сбой между бизнес-записью
и завершением задачи может привести к повторному выполнению.

Чтобы отключить задачу, остановите оба экземпляра и запустите оба с `JOB_ENABLED=false`.
После завершения уже запущенных задач счётчик перестаёт расти. Для включения удалите
`JOB_ENABLED` и перезапустите оба экземпляра. Настройка должна совпадать на всех экземплярах.

Остановить PostgreSQL после завершения работы приложений:

```shell
docker compose down
```

## Test

Протестировать локально:

```shell
./gradlew test
```

Для ручной проверки запустите два экземпляра, проверьте рост `/ticks`, остановите один
и убедитесь, что счётчик продолжает расти. Затем проверьте отключение задачи на обоих экземплярах.
