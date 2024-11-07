package ru.tinkoff.kora.example.submodule.app;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.Network;
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.example.submodule.app.TestApplication.TestCategoryRepository;
import ru.tinkoff.kora.example.submodule.app.TestApplication.TestPetRepository;
import ru.tinkoff.kora.example.submodule.pet.model.dao.Pet;
import ru.tinkoff.kora.example.submodule.pet.service.PetService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

/**
 * Тестовый контейнер приложение, который расширяет основное приложение и например добавляет
 * некоторые компоненты
 * из общих модулей, которые не используются в данном приложении, которые могут быть например
 * использованы в других подобных приложениях.
 * Например, когда у вас есть разные приложения READ API и WRITE API.
 * Либо, вам нужны некоторые функции сохранения/удаления/обновления только для тестирования в
 * качестве быстрой тестовой утилиты.
 * <p>
 * Но мы НАСТОЯТЕЛЬНО РЕКОМЕНДУЕМ ТЕСТИРОВАТЬ приложения как черный ящик в качестве основного
 * подхода.
 * -------
 * Test Application than extends Real Application and may be adds some components
 * from common modules that are not used in Real App, but may be used in other similar apps.
 * Like when you have different READ API application and WRITE API application
 * or may be, you need some save/delete/update methods only for testing as fast test utils.
 * <p>
 * But we STRONGLY ENCOURAGE AND RECOMMEND TO USE black box testing as a primary source of truth for
 * tests.
 */
@TestcontainersPostgreSQL(
        network = @Network(shared = true),
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                engine = Migration.Engines.FLYWAY,
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(TestApplication.class)
class IntegrationTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private PetService petService;

    @TestComponent
    private TestPetRepository testPetRepository;
    @TestComponent
    private TestCategoryRepository testCategoryRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                db {
                  jdbcUrl = ${POSTGRES_JDBC_URL}
                  username = ${POSTGRES_USER}
                  password = ${POSTGRES_PASS}
                  poolName = "kora"
                }
                pet-cache.maximumSize = 0
                resilient {
                   circuitbreaker.pet {
                     slidingWindowSize = 2
                     minimumRequiredCalls = 2
                     failureRateThreshold = 100
                     permittedCallsInHalfOpenState = 1
                     waitDurationInOpenState = 15s
                   }
                   timeout.pet.duration = 5000ms
                   retry.pet {
                     delay = 100ms
                     attempts = 0
                   }
                 }""")
                .withSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void updatePetWithNewCategoryCreated() {
        // given
        var added = petService.add("dog", "dog");
        assertEquals(1, added.id());
        assertEquals(1, added.category().id());

        // when
        var updated = petService.update(added.id(), "cat", "cat", Pet.Status.PENDING);
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().id());
        assertEquals(2, updated.get().category().id());

        // then
        assertEquals(1, testPetRepository.findAll().size());
        assertEquals(2, testCategoryRepository.findAll().size());
    }

    @Test
    void updatePetWithSameCategory() {
        // given
        var added = petService.add("dog", "dog");
        assertEquals(1, added.id());
        assertEquals(1, added.category().id());

        // when
        var updated = petService.update(added.id(), "cat", "dog", Pet.Status.PENDING);
        assertTrue(updated.isPresent());
        assertNotEquals(0, updated.get().id());
        assertNotEquals(0, updated.get().category().id());

        // then
        assertEquals(1, testPetRepository.findAll().size());
        assertEquals(1, testCategoryRepository.findAll().size());
    }
}
