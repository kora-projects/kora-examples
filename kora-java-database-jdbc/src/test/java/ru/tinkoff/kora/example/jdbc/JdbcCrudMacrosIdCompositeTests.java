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
class JdbcCrudMacrosIdCompositeTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private JdbcCrudMacrosIdCompositeRepository repository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void syncSingle() {
        // given
        var id = new JdbcCrudMacrosIdCompositeRepository.Entity.EntityId();
        var entityCreate = new JdbcCrudMacrosIdCompositeRepository.Entity(
                id, "2");
        repository.insert(entityCreate);

        var foundCreated = repository.findById(id).orElseThrow();
        assertNotNull(foundCreated);
        assertEquals(id, foundCreated.id());
        assertEquals("2", foundCreated.name());

        // when
        var entityUpdate = new JdbcCrudMacrosIdCompositeRepository.Entity(
                id, "6");
        repository.update(entityUpdate);

        var foundUpdated = repository.findById(id).orElseThrow();
        assertNotNull(foundUpdated);
        assertEquals(id, foundUpdated.id());
        assertEquals("6", foundUpdated.name());

        // then
        repository.deleteById(id);
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void syncBatch() {
        // given
        var id1 = new JdbcCrudMacrosIdCompositeRepository.Entity.EntityId();
        var id2 = new JdbcCrudMacrosIdCompositeRepository.Entity.EntityId();
        var entityCreate1 = new JdbcCrudMacrosIdCompositeRepository.Entity(id1, "2");
        var entityCreate2 = new JdbcCrudMacrosIdCompositeRepository.Entity(id2, "4");
        assertEquals(2, repository.insertBatch(List.of(entityCreate1, entityCreate2)).value());

        var foundCreated = repository.findAll();
        assertEquals(2, foundCreated.size());
        for (var entity : foundCreated) {
            if (entity.id().equals(id1)) {
                assertEquals(id1, entity.id());
                assertEquals("2", entity.name());
            } else {
                assertEquals(id2, entity.id());
                assertEquals("4", entity.name());
            }
        }

        // when
        var entityUpdate1 = new JdbcCrudMacrosIdCompositeRepository.Entity(id1, "6");
        var entityUpdate2 = new JdbcCrudMacrosIdCompositeRepository.Entity(id2, "8");
        assertEquals(2, repository.updateBatch(List.of(entityUpdate1, entityUpdate2)).value());

        var foundUpdated = repository.findAll();
        assertEquals(2, foundUpdated.size());
        for (var entity : foundUpdated) {
            if (entity.id().equals(id1)) {
                assertEquals(id1, entity.id());
                assertEquals("6", entity.name());
            } else {
                assertEquals(id2, entity.id());
                assertEquals("8", entity.name());
            }
        }

        var entityUpsert1 = new JdbcCrudMacrosIdCompositeRepository.Entity(id1, "8");
        var entityUpsert2 = new JdbcCrudMacrosIdCompositeRepository.Entity(id2, "10");
        assertEquals(2, repository.upsertBatch(List.of(entityUpsert1, entityUpsert2)).value());

        var foundUpserted = repository.findAll();
        assertEquals(2, foundUpserted.size());
        for (var entity : foundUpserted) {
            if (entity.id().equals(id1)) {
                assertEquals(id1, entity.id());
                assertEquals("8", entity.name());
            } else {
                assertEquals(id2, entity.id());
                assertEquals("10", entity.name());
            }
        }

        // then
        assertEquals(2, repository.deleteAll().value());
        assertTrue(repository.findAll().isEmpty());
    }
}
