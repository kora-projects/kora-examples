# Kora Java Distributed Resilient

Пример распределённых Rate Limiter и Retry Budget в Kora с общим состоянием в Redis.

В примере использовались модули:
- [Resilient](https://kora-projects.github.io/kora-docs/ru/documentation/resilient/)
- [HTTP Server](https://kora-projects.github.io/kora-docs/ru/documentation/http-server/)
- [Config Hocon](https://kora-projects.github.io/kora-docs/ru/documentation/config-hocon/)

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

Запустить Redis из каталога примера:

```shell
docker compose up -d --wait
```

Запустить локально:

```shell
./gradlew run
```

Конфигурация находится в `src/main/resources/application.conf`. `REDIS_URL` задаёт адрес Redis,
`HTTP_PORT` и `SYSTEM_PORT` — порты приложения. `KEY_PREFIX` переопределяет общий префикс;
`distributedRetryBudget` получает его через HOCON и добавляет `:budget`. Префикс и TTL бюджета
передаются в `BudgetModule` через `RetryBudgetConfig`, без прямого чтения environment в Java.

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

`POST /permit` использует общую квоту: четыре разрешения в час на оба экземпляра.
`POST /fail` всегда завершается ошибкой и расходует общий бюджет повторов через
`@Retryable(PartnerRetry.class)`. Начальный бюджет — два токена; `GET /attempts`
показывает число downstream-попыток конкретного экземпляра. Успешные операции пополняют бюджет
согласно `ratio`; пример с ошибками его не пополняет. Ошибки подключения к Redis передаются вызывающему коду.

Остановить Redis после завершения работы приложений:

```shell
docker compose down
```

## Test

Протестировать локально:

```shell
./gradlew test
```

Для ручной проверки распределённого состояния запустите два экземпляра с одинаковым `KEY_PREFIX`.
На свежем состоянии Redis четыре разрешения доступны суммарно. Два вызова `/fail` дают четыре
попытки суммарно: два исходных вызова и два повтора из общего бюджета. Проверяйте квоту вне границы
часа; для повторной проверки используйте новый `KEY_PREFIX` на обоих экземплярах.
