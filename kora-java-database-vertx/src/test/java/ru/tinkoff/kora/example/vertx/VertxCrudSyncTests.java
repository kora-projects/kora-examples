package ru.tinkoff.kora.example.vertx;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ContainerPostgresConnection;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgres;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersPostgres(
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                engine = Migration.Engines.FLYWAY,
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class VertxCrudSyncTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private VertxCrudSyncRepository repository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        var params = connection.params();
        return KoraConfigModification
                .ofSystemProperty("POSTGRES_VERTX_URI",
                        "postgresql://%s:%s/%s".formatted(params.host(), params.port(), params.database()))
                .withSystemProperty("POSTGRES_USER", params.username())
                .withSystemProperty("POSTGRES_PASS", params.password());
    }

    @Test
    void monoSingleSuccess() {
        // given
        var entityCreate = new VertxCrudSyncRepository.Entity("1", 1, "2", null);
        repository.insert(entityCreate);

        var foundCreated = repository.findById("1");
        assertNotNull(foundCreated);
        assertEquals("1", foundCreated.id());
        assertEquals(1, foundCreated.field1());
        assertEquals("2", foundCreated.value2());
        assertNull(foundCreated.value3());

        // when
        var entityUpdate = new VertxCrudSyncRepository.Entity("1", 5, "6", null);
        repository.update(entityUpdate);

        var foundUpdated = repository.findById("1");
        assertNotNull(foundUpdated);
        assertEquals("1", foundUpdated.id());
        assertEquals(5, foundUpdated.field1());
        assertEquals("6", foundUpdated.value2());
        assertNull(foundUpdated.value3());

        // then
        repository.deleteById("1");
        assertNull(repository.findById("1"));
    }

    @Test
    void monoBatchSuccess() {
        // given
        var entity1 = new VertxCrudSyncRepository.Entity("1", 1, "2", null);
        var entity2 = new VertxCrudSyncRepository.Entity("2", 3, "4", null);
        assertEquals(2L, repository.insertBatch(List.of(entity1, entity2)).value());

        var foundCreated = repository.findAll();
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
        var entityUpdate1 = new VertxCrudSyncRepository.Entity("1", 5, "6", null);
        var entityUpdate2 = new VertxCrudSyncRepository.Entity("2", 7, "8", null);
        assertEquals(2L, repository.updateBatch(List.of(entityUpdate1, entityUpdate2)).value());

        var foundUpdated = repository.findAll();
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
        assertEquals(2L, repository.deleteAll().value());
        assertTrue(repository.findAll().isEmpty());
    }
}
