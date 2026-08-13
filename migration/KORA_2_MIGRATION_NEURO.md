# Kora 1.x → 2.0: инструкции для нейро-агентов

Документ адресован нейро-агенту (AI-agent), мигрирующему **чужой** проект, и покрывает только те трансформации,
которые нельзя выполнить безопасно через OpenRewrite или детерминированный скрипт.

Разделение ответственности:

| Слой | Что делает | Где описан |
|---|---|---|
| OpenRewrite | Java AST: пакеты, типы, координаты зависимостей | `migration/openrewrite/` |
| Скрипт `migrate_kora_2.py` | текстовые/файловые замены: Gradle, Kotlin, ресурсы, перенос каталогов пакетов | `migration/scripts/` |
| **Нейро-агент (этот файл)** | смысловые изменения, требующие анализа окружающего кода | ниже |
| Человек | архитектурные решения по модулям с удалённой функциональностью | `KORA_2_MIGRATION_STATUS.md` |

Прежде чем применять паттерн — прочитайте соответствующий раздел языкового гайда
(`KORA_2_JAVA_MIGRATION_GUIDE.md` / `KORA_2_KOTLIN_MIGRATION_GUIDE.md`).

---

## Migration pattern: строковые resilient-аннотации → типизированные спецификации

### Applies when

В коде есть `@CircuitBreaker("name")`, `@Retry("name")`, `@Timeout("name")`, `@Fallback(value = "name", …)`.

### Search for

- Java: `@CircuitBreaker("`, `@Retry("`, `@Timeout("`, `@Fallback(value`
- Kotlin: то же
- Конфигурация: секции `resilient.circuitbreaker.*`, `resilient.retry.*`, `resilient.timeout.*` в `application.conf` / `application.yaml`

### Required analysis

1. Собрать все строковые имена и сопоставить каждому путь в конфигурации. Имя в аннотации 1.x — это **последний сегмент** пути конфигурации (`@CircuitBreaker("pet")` → `resilient.circuitbreaker.pet`).
2. Проверить, используется ли одно имя в нескольких классах: тогда spec-интерфейс должен быть один и общий, а не по копии на класс.
3. Найти секцию конфигурации: если её нет, значение бралось по умолчанию — секцию всё равно нужно создать или убедиться, что дефолты 2.0 подходят.
4. Проверить тип окна circuit breaker: в 2.0 для окна фиксированного размера обязателен `type = FIXED_WINDOW`.

### Target Kora 2.0 design

Отдельный интерфейс-спецификация на каждое логическое имя, привязанный к пути конфигурации, и аннотация, принимающая класс этого интерфейса.

### Java migration

```java
// Было
@CircuitBreaker("pet")
@Retry("pet")
@Timeout("pet")
public Optional<Pet> findById(long id) { … }

// Стало
@CircuitBreakable(PetCircuitBreaker.class)
@Retryable(PetRetry.class)
@Timeout(PetTimeouter.class)
public Optional<Pet> findById(long id) { … }
```

Новые типы (обычно рядом с сервисом, который их использует):

```java
@CircuitBreakerSpec("resilient.circuitbreaker.pet")
public interface PetCircuitBreaker extends CircuitBreaker {}

@RetrySpec("resilient.retry.pet")
public interface PetRetry extends Retry {}

@TimeoutSpec("resilient.timeout.pet")
public interface PetTimeouter extends Timeouter {}
```

Импорты: `io.koraframework.resilient.circuitbreaker.annotation.{CircuitBreakable, CircuitBreakerSpec}`,
`io.koraframework.resilient.retry.annotation.{Retryable, RetrySpec}`,
`io.koraframework.resilient.timeout.annotation.{Timeout, TimeoutSpec}`,
базовые типы — `io.koraframework.resilient.circuitbreaker.CircuitBreaker`, `…retry.Retry`, `…timeout.Timeouter`.

### Kotlin migration

```kotlin
@CircuitBreakable(PetCircuitBreaker::class)
@Retryable(PetRetry::class)
@Timeout(PetTimeouter::class)
fun findById(id: Long): Pet?

@CircuitBreakerSpec("resilient.circuitbreaker.pet")
interface PetCircuitBreaker : CircuitBreaker

@RetrySpec("resilient.retry.pet")
interface PetRetry : Retry

@TimeoutSpec("resilient.timeout.pet")
interface PetTimeouter : Timeouter
```

### Gradle changes

Никаких, кроме общей смены координат: артефакт остаётся `io.koraframework:resilient-kora`.

### Configuration changes

```hocon
resilient {
  circuitbreaker.pet {
    type = FIXED_WINDOW
    countBased.windowSize = 50      # было slidingWindowSize
    minimumRequiredCalls = 25
    failureRateThreshold = 50
    waitDurationInOpenState = "25s"
    permittedCallsInHalfOpenState = 10
  }
  timeout.pet.duration = "5000ms"
  retry.pet { delay = "500ms"; delayStep = "5s"; attempts = 3 }
}
```

### Test migration

Тесты, переопределяющие конфигурацию отказоустойчивости (`KoraConfigModification.ofString`), должны использовать тот же путь и содержать `type = FIXED_WINDOW`.

### Common mistakes

- Создавать по spec-интерфейсу на каждый метод — нужен один на логическое имя.
- Забыть блок `countBased` и получить NPE на инициализации графа: в `CircuitBreakerConfig` он объявлен `@Nullable`, но реализация по умолчанию (`StripedApproxKoraCircuitBreaker`) разыменовывает `config.countBased()` без проверки. Сам `type` при этом опционален — по умолчанию `STRIPED_APPROX`.
- Оставить `@Fallback(value = "…")` — атрибут `value` удалён, остаётся только `method`.
- Заменить `@Timeout("pet")` на `@Timeoutable(...)` — такой аннотации нет, имя `@Timeout` сохранилось, изменился только тип атрибута.

### Validation

`./gradlew <module>:classes` — исчезает `incompatible types: String cannot be converted to Class<? extends Timeouter>`.
Для Kotlin дополнительно исчезает краш `kspKotlin` с `ClassCastException: String → KSType`.

### Escalate when

Одно строковое имя используется с разными наборами параметров в разных местах — это архитектурное решение (одна спецификация или несколько), его принимает человек.

---

## Migration pattern: конфигурация, зашитая в исходники тестов

### Applies when

Тест переопределяет конфигурацию графа через `KoraConfigModification.ofString("""…""")`,
и внутри текстового блока лежит HOCON с ключами 1.x.

### Search for

```
rg -n 'ofString\(' --type java --type kotlin
rg -n 'slidingWindowSize|publicApiHttpPort|privateApiHttpPort|^\s*db\s*\{' -g '*.java' -g '*.kt'
```

### Required analysis

Это самый неприятный класс расхождений: ресурсные `.conf` уже мигрированы, код компилируется,
а тест падает на инициализации графа — иногда с внятным `ConfigValueException`, иногда
(как с circuit breaker) с NPE в реализации. Скрипты, которые сканируют только `.conf` и `.yaml`,
такие места не видят по определению.

Проверять нужно те же три вещи, что и в ресурсах:

- секция датасорса `db` → `jdbc`;
- порты `publicApiHttpPort` / `privateApiHttpPort` → `port` / `system.port`;
- окно circuit breaker `slidingWindowSize` → `countBased.windowSize`.

### Target Kora 2.0 design

Внутри текстового блока — ровно тот же HOCON, что и в ресурсах модуля. Расхождение между
`src/main/resources/application.conf` и тестовым блоком само по себе подозрительно: если тест
переопределяет секцию целиком, он обязан использовать актуальные имена ключей.

### Java migration

```java
return KoraConfigModification.ofString("""
        jdbc {
          jdbcUrl = ${POSTGRES_JDBC_URL}
          username = ${POSTGRES_USER}
          password = ${POSTGRES_PASS}
        }
        resilient.circuitbreaker.pet {
          type = FIXED_WINDOW
          countBased.windowSize = 2
        }
        """);
```

### Kotlin migration

Идентично: в Kotlin тот же разделитель `"""`, отличается только синтаксис вокруг.

### Gradle changes

Нет.

### Test migration

Это и есть миграция теста. Отдельно стоит проверить `withSystemProperty(...)`: имена
переменных окружения меняться не обязаны, но подставляются они уже в новые ключи.

### Common mistakes

- Прогнать скрипт миграции конфигурации и решить, что конфигурация мигрирована целиком.
- Применить правила конфигурации ко всему `.java`/`.kt` файлу: `int slidingWindowSize = 2;`
  внешне похож на строку HOCON. Переписывать можно только содержимое текстового блока.
- Починить `application.conf` и забыть, что у теста своя копия секции.

### Validation

`./gradlew <module>:test` — граф инициализируется. Компиляция дефект не ловит вообще.

### Escalate when

Тестовый блок содержит ключи, которых нет ни в ресурсах модуля, ни в конфигурации 2.0 —
значит тест полагается на что-то удалённое, и нужно решение о смысле теста.

---

## Migration pattern: снятие `suspend` по всей цепочке вызовов (Kotlin)

### Applies when

Проект использует `suspend`-репозитории Kora, корутинные контракты Kora или реактивные типы исключительно ради неблокирующего доступа к БД.

### Search for

- `suspend fun` в интерфейсах с `@Repository`
- `runBlocking`, `runTest`, `coEvery`, `Dispatchers.IO` вокруг вызовов Kora
- `Mono`/`Flux` в сигнатурах, приходящих из репозиториев Kora

### Required analysis

1. Построить полный граф вызовов от `suspend`-метода репозитория вверх — до контроллера/слушателя/теста. Снятие `suspend` обязано пройти по всей цепочке; частичная правка оставит несобираемый код.
2. Отдельно выделить места, где корутины использовались ради параллелизма (`coroutineScope`,
   `supervisorScope`, `async`/`await`, `awaitAll`, structured cancellation/timeouts). Они не
   остаются в application service: их семантика переводится на Java `StructuredTaskScope`.
   Использовать последний GA JDK и последнюю preview-итерацию этого JDK; включить
   `--enable-preview` для compile, test и каждого runtime launch. В `@HttpClient` interface
   нельзя оставлять даже default `suspend`-обёртки над blocking-методом.
   Для Kotlin compilation дополнительно задать `-Xjdk-release=<latest-GA-major>` и
   `-Xjvm-enable-preview`; одного `JavaCompile.compilerArgs += --enable-preview` недостаточно
   для описания Kotlin-задач. Не переносить названия API из предыдущего preview JDK.
3. Проверить транзакции: код, полагавшийся на корутинный контекст транзакции, требует пересборки границ.
4. Проверить отмену: `withTimeout`/отмена скоупа больше не прерывает операцию БД.

### Target Kora 2.0 design

Синхронные контракты, исполняемые на виртуальных потоках. Никаких `Dispatchers.IO` вокруг вызовов Kora — виртуальные потоки уже решают задачу блокирующего I/O.

### Kotlin migration

```kotlin
// Было
@Repository
interface PetRepository : JdbcRepository {
    suspend fun findById(id: Long): Pet?
}

class PetService(private val repository: PetRepository) {
    suspend fun get(id: Long): Pet? = withContext(Dispatchers.IO) { repository.findById(id) }
}

// Стало
@Repository
interface PetRepository : JdbcRepository {
    fun findById(id: Long): Pet?
}

class PetService(private val repository: PetRepository) {
    fun get(id: Long): Pet? = repository.findById(id)
}
```

### Java migration

Неприменимо (паттерн Kotlin-специфичный). Java-аналог — снятие `CompletionStage`/реактивных обёрток с тем же анализом транзакций и отмены.

### Test migration

`runTest { … }` вокруг ставших синхронными вызовов убирается; `coEvery` → `every`, `coVerify` → `verify`. Тест, который проверял отмену корутины, теряет смысл — его нужно переписать или удалить с обоснованием, а не «ослабить».

### Common mistakes

- Механически убрать `suspend`, оставив `runBlocking` в вызывающем коде.
- Оставить `withContext(Dispatchers.IO)` вокруг синхронного вызова Kora — лишний переброс между пулами.
- Снять `suspend` с метода, который использовал корутины ради параллелизма, и потерять параллельность.

### Validation

`./gradlew <module>:compileKotlin <module>:compileTestKotlin` и прогон тестов модуля.

### Escalate when

В цепочке есть параллелизм, отмена по таймауту или supervisor semantics — разработчик обязан
выбрать эквивалентный `StructuredTaskScope.Joiner`/timeout policy и покрыть failure/cancellation
тестами. Каналы/Flow не имеют механического аналога в `StructuredTaskScope` и требуют отдельного
редизайна; сохранять Kotlin Structured Concurrency как обход миграции нельзя.

---

## Migration pattern: мапперы, интерцепторы и мапперы ключей кеша должны быть DI-компонентами

### Applies when

Класс упоминается в `@Mapping(X.class)` / `@InterceptWith(X.class)`, но сам не является компонентом графа.

### Search for

`@Mapping(`, `@InterceptWith(` — и для каждого аргумента проверить, есть ли на классе `@Component` либо фабричный метод в `@Module`.

### Required analysis

Проверить, не предоставляется ли класс уже методом модуля — тогда `@Component` добавлять не нужно (получите неоднозначность). Учесть вложенные классы: они тоже должны быть компонентами.

### Target Kora 2.0 design

Генерируемый модуль контроллера/аспекта **инжектит** маппер как зависимость, а не создаёт его сам, поэтому маппер обязан присутствовать в графе.

### Java migration

```java
@Component                                    // ← добавлено
public static final class UserContextRequestMapper implements HttpServerRequestMapper<UserContext> { … }
```

### Kotlin migration

```kotlin
@Component
class UserContextMapping : CacheKeyMapper<…> { … }
```

### Common mistakes

- Пометить `@Component` класс, который уже отдаётся методом модуля → неоднозначность в графе.
- Пропустить вложенные классы: сообщение об ошибке называет внешний класс через точку (`Controller.Mapper`), что легко принять за внешний тип.
- Разметить `@Component` **все** мапперы подряд. Признак — конструктор: маппер без зависимостей
  Kora создаёт сама, и такой класс, помеченный `@Component`, даёт `Multiple components match`.
  Маппер с зависимостями (типичный случай — `JsonReader<T>` внутри `@ResponseCodeMapper`)
  создать самостоятельно она не может, и без `@Component` получается
  `No component found for dependency` на его собственный тип.
- Сослаться на маппер через `@Mapping`, когда метод возвращает `HttpResponseEntity<T>`:
  `@Mapping` заставляет маппер производить **весь** тип возврата, а шаблонная фабрика
  фреймворка ожидает маппер полезной нагрузки и оборачивает его в entity сама.
  Симптом — `incompatible types: T cannot be converted to HttpResponseEntity<T>`.

### Validation

Исчезает `No component found for dependency: … (no tags)` при сборке.

### Escalate when

Маппер требует конструктора с параметрами, которых нет в графе — нужно решение о том, как их предоставить.

---

## Migration pattern: расстановка JSpecify type-use аннотаций

### Applies when

После автозамены `jakarta.annotation.Nullable` → `org.jspecify.annotations.Nullable` компилятор выдаёт
`type annotation @org.jspecify.annotations.Nullable is not expected here`.

### Required analysis

JSpecify-аннотации — **type-use**. Позиция в объявлении значима, особенно для:

- квалифицированных вложенных типов: `Outer.@Nullable Inner`, а не `@Nullable Outer.Inner`;
- массивов: `String @Nullable []` (nullable массив) против `@Nullable String []` (массив nullable-строк);
- дженериков: `List<@Nullable String>`.

Такое же поведение у некоторых аннотаций Kora (встречалось `type annotation @Column("status") is not expected here`).

### Kotlin migration

В Kotlin аннотации нуллабельности **удаляются**, нуллабельность выражается типом `T?`. `@field:Nullable` под Kotlin 2.4 — некорректная цель.

### Common mistakes

- Просто удалить аннотацию, чтобы «прошла компиляция», потеряв контракт нуллабельности. Если тип действительно nullable — аннотацию нужно поставить правильно, а не убрать.

### Validation

Компиляция модуля; для API — сверка сгенерированных сигнатур с ожидаемыми.

### Escalate when

Требуется решение о переходе на `@NullMarked` на уровне пакета/модуля — это меняет контракт всего кода.

---

## Diagnostic pattern: краш KSP-процессора — дефект фреймворка или немигрированный код?

### Applies when

`kspKotlin` падает с `e: [ksp] <исключение>` без указания файла и строки.

### Required analysis

Не считайте краш дефектом фреймворка сразу. Порядок:

1. Проверить, не остался ли в модуле код 1.x, который процессор не ожидает. Подтверждённый пример: строковый `@CircuitBreaker("pet")` вызывает `ClassCastException: String → KSType` — первопричина в коде, а не в процессоре.
2. Смигрировать модуль по языковому гайду и перезапустить `kspKotlin`.
3. Если краш воспроизводится на корректном коде 2.0 — минимизировать репродукцию (один класс, одна аннотация) и завести запись в `KORA_2_FRAMEWORK_ISSUES.md`.
4. Только после минимизации предлагать фикс во фреймворке.

### Common mistakes

- Записать краш как баг фреймворка, не смигрировав код (ложное срабатывание).
- Считать краш «своей виной» и переписывать корректный код 2.0 в обход (маскирует реальный дефект).

### Escalate when

Краш воспроизводится на минимальном корректном примере — это работа по фреймворку, а не по приложению.

---

## Diagnostic pattern: фантомные ошибки `package ru.tinkoff.kora.* does not exist`

### Applies when

Компилятор ругается на пакеты 1.x в файлах, которых **нет** в исходниках; пути ведут в `build/generated/…`.

### Required analysis

Это не миграционная ошибка. Задачи-генераторы (OpenAPI, protobuf, `wsdl2java`) не удаляют предыдущий вывод, а ключ build-кеша не учитывает смену `apiPackage`/`modelPackage` — в одном source set оказываются старый и новый пакеты.

### Fix

```shell
./gradlew clean --continue
./gradlew classes testClasses --continue --no-build-cache
```

### Common mistakes

- Начать «чинить» сгенерированные файлы руками. Править сгенерированный код как постоянное решение нельзя.
- Добавлять зависимости, чтобы «найти» несуществующий пакет 1.x.

### Validation

После `clean` + `--no-build-cache` все упоминания `ru.tinkoff.kora` из вывода компилятора исчезают. Если остаются — значит, ссылка действительно есть в исходниках или в конфигурации генерации (например, `ru.tinkoff.grpc.client` в настройках protobuf).

---

## Migration pattern: собственные метаданные native-image при переименовании пакетов

### Applies when

В модуле есть `src/main/resources/META-INF/native-image/<что-то>/` и проект меняет группу/пакеты.

### Search for

```shell
find . -path "*/META-INF/native-image/*" -type d
find . -name "reflection-config.json"      # такого имени native-image не знает вовсе
```

### Required analysis

Два независимых вопроса, которые легко спутать:

1. **Имя каталога** после `native-image/` — это просто пространство имён, обычно `<groupId>/<artifactId>`.
   На работоспособность оно не влияет, но оставлять старую группу после миграции нельзя — это единственное,
   что разводит метаданные разных библиотек по разным каталогам в одном jar.
2. **Имена файлов внутри** — вот здесь ошибка смертельная и тихая. Читаются только
   `reflect-config.json`, `resource-config.json`, `proxy-config.json`, `serialization-config.json`,
   `jni-config.json`, `native-image.properties`, `reachability-metadata.json`. Файл с любым другим именем
   (классика — `reflection-config.json`) просто игнорируется: ошибки нет, предупреждения нет,
   регистраций тоже нет. В самом Kora 2.0 три модуля жили с таким файлом
   ([#814](https://github.com/kora-projects/kora/pull/814)).

### Target design

Каталог переименован под новую группу, имена файлов каноничные, содержимое **не трогается**.

```
META-INF/native-image/ru.tinkoff.kora.examples/logback/reflect-config.json
→ META-INF/native-image/io.koraframework.examples/logback/reflect-config.json
```

### Gradle changes

Нет.

### Common mistakes

- Удалить метаданные «за ненадобностью», потому что приложение работает на JVM. JVM их не читает вообще —
  такая проверка ничего не доказывает.
- Править содержимое (имена классов внутри) вместе с переименованием каталога: в метаданных
  перечислены классы **библиотек** (logback, XNIO), а они не переименовывались.

### Validation

`./gradlew <module>:nativeCompile` и запуск бинаря. Для logback признак потерянных метаданных —
приложение стартует, но логи пусты или идут мимо конфигурации.

---

## Diagnostic pattern: native-image собрался, но падает в рантайме

### Applies when

`nativeCompile` завершился успешно, а бинарь падает на старте или ведёт себя иначе, чем на JVM.
Типичные формулировки: `No XNIO provider found`, `ClassNotFoundException` на класс, который точно
в classpath, `/metrics` отвечает 500, кеш падает на создании.

### Required analysis

Главное правило: **текст исключения здесь часто врёт**. Библиотеки ловят `Throwable` при поиске
провайдеров и репортят вторичную ошибку. Канонический пример: `No XNIO provider found` означал
не отсутствие провайдера, а невозможность создать его логгер — jboss-logging грузит реализацию
`<интерфейс>_$logger` рефлексивно ([#811](https://github.com/kora-projects/kora/pull/811)).

Порядок работы — три шага, каждый даёт факт, а не гипотезу.

**Шаг 1. Трассирующий агент** — что реально грузится рефлексивно. Запускать на GraalVM-JDK
тот же shadow-jar, что идёт в образ, и прогнать сценарий приложения целиком:

```shell
java -agentlib:native-image-agent=config-output-dir=/tmp/na-config \
     -jar build/libs/<module>-all.jar
# прогнать все сценарии, остановить штатно (SIGTERM), иначе конфиг не запишется
diff -r /tmp/na-config src/main/resources/META-INF/native-image/<group>/
```

Агент видит только те ветки, которые были выполнены — это ограничение, а не баг. Его вывод —
гипотеза для шага 3, а не готовый патч: вкладывать его целиком в ресурсы приложения нельзя —
там будут сотни записей про чужие библиотеки, место которым — в этих библиотеках.

**Шаг 2. Минимальный native-пробник** — развести «дефект фреймворка» и «ошибка приложения».
Отдельный `main`, который дёргает только подозреваемую библиотеку без Kora, собранный тем же
`native-image`. Если пробник падает — причина в библиотеке или в метаданных модуля Kora,
и искать её в коде приложения бессмысленно.

**Шаг 3. Проверить, что метаданные вообще читаются.** Этот шаг пропускают чаще всего,
а он один отличает «запись неполная» от «файл не читается». Схема эксперимента:

1. добавить в проверяемый файл заведомо наблюдаемую регистрацию (метод, который точно вызовется);
2. пересобрать образ и посмотреть, изменилось ли поведение;
3. если нет — положить ту же запись в файл с каноническим именем и повторить.

Именно так выяснилось, что `reflection-config.json` не читается: добавление метода в старый файл
не дало ничего, та же запись в `reflect-config.json` — сработала.

### Common mistakes

- **Поверить тексту исключения** и искать то, чего в ошибке написано (провайдер, который на самом
  деле на месте). Сначала убедитесь, что исключение не вторичное после `catch (Throwable)`.
- **Считать успешную сборку признаком готовности.** Все пять найденных при миграции
  native-дефектов давали зелёную сборку.
- **Ждать строку в логе как признак старта** (в тестах или вручную). Формулировки меняются между
  версиями, а событие может быть вообще потеряно аппендером
  ([#816](https://github.com/kora-projects/kora/pull/816)). Ждите `/system/readiness`.
- **Записать вывод агента в ресурсы приложения целиком.** Метаданные чужой библиотеки — её ответственность;
  в приложении они застынут и разойдутся с обновлённой библиотекой.
- **Проверять фикс только через `nativeCompile`.** Если в проекте есть ещё и сборка в Docker,
  пути получения метаданных у них разные — проверять надо оба.

### Validation

Бинарь стартует; `/system/readiness` отвечает 200; `/metrics` отдаёт метрики; сценарий приложения
отрабатывает против реальной зависимости; в логе нет стектрейсов при старте.

### Escalate when

Минимальный пробник (шаг 2) воспроизводит падение без кода приложения — это работа по фреймворку
или по библиотеке, а не по миграции. Заводите запись в `KORA_2_FRAMEWORK_ISSUES.md`
с указанием цифр до/после — модульный тест на такой дефект написать нельзя: на JVM он
не воспроизводится вообще.
