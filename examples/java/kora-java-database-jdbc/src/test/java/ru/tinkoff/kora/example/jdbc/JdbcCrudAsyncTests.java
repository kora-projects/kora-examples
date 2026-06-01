package ru.tinkoff.kora.example.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersPostgreSQL(
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                engine = Migration.Engines.FLYWAY,
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class JdbcCrudAsyncTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private JdbcCrudAsyncRepository repository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void asyncSingle() {
        // given
        var entityCreate = new JdbcCrudAsyncRepository.Entity("1", 1, "2", null);
        repository.insert(entityCreate).toCompletableFuture().join();

        var foundCreated = repository.findById("1").toCompletableFuture().join();
        assertNotNull(foundCreated);
        assertEquals("1", foundCreated.id());
        assertEquals(1, foundCreated.field1());
        assertEquals("2", foundCreated.value2());
        assertNull(foundCreated.value3());

        // when
        var entityUpdate = new JdbcCrudAsyncRepository.Entity("1", 5, "6", null);
        repository.update(entityUpdate).toCompletableFuture().join();

        var foundUpdated = repository.findById("1").toCompletableFuture().join();
        assertNotNull(foundUpdated);
        assertEquals("1", foundUpdated.id());
        assertEquals(5, foundUpdated.field1());
        assertEquals("6", foundUpdated.value2());
        assertNull(foundUpdated.value3());

        // then
        repository.deleteById("1").join();
        assertNull(repository.findById("1").toCompletableFuture().join());
    }

    @Test
    void asyncBatch() {
        // given
        var entityCreate1 = new JdbcCrudAsyncRepository.Entity("1", 1, "2", null);
        var entityCreate2 = new JdbcCrudAsyncRepository.Entity("2", 3, "4", null);
        assertEquals(2L, repository.insertBatch(List.of(entityCreate1, entityCreate2)).toCompletableFuture().join().value());

        var foundCreated = repository.findAll().toCompletableFuture().join();
        assertNotNull(foundCreated);
        assertEquals(2, foundCreated.size());
        for (var entity : foundCreated) {
            if (entity.id().equals("1")) {
                assertEquals("1", entity.id());
                assertEquals(1, entity.field1());
                assertEquals("2", entity.value2());
                assertNull(entity.value3());
            } else {
                assertEquals("2", entity.id());
                assertEquals(3, entity.field1());
                assertEquals("4", entity.value2());
                assertNull(entity.value3());
            }
        }

        // when
        var entityUpdate1 = new JdbcCrudAsyncRepository.Entity("1", 5, "6", null);
        var entityUpdate2 = new JdbcCrudAsyncRepository.Entity("2", 7, "8", null);
        assertEquals(2L, repository.updateBatch(List.of(entityUpdate1, entityUpdate2)).toCompletableFuture().join().value());

        var foundUpdated = repository.findAll().toCompletableFuture().join();
        assertEquals(2, foundUpdated.size());
        for (var entity : foundUpdated) {
            if (entity.id().equals("1")) {
                assertEquals("1", entity.id());
                assertEquals(5, entity.field1());
                assertEquals("6", entity.value2());
                assertNull(entity.value3());
            } else {
                assertEquals("2", entity.id());
                assertEquals(7, entity.field1());
                assertEquals("8", entity.value2());
                assertNull(entity.value3());
            }
        }

        // then
        assertEquals(2L, repository.deleteAll().join().value());
        assertTrue(repository.findAll().toCompletableFuture().join().isEmpty());
    }
}
