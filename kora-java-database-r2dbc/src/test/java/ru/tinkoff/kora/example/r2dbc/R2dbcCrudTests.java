package ru.tinkoff.kora.example.r2dbc;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL;
import java.time.Duration;
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
class R2dbcCrudTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private R2dbcCrudRepository repository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        var params = connection.params();
        var url = "r2dbc:postgresql://%s:%s/%s".formatted(params.host(), params.port(), params.database());
        return KoraConfigModification.ofSystemProperty("POSTGRES_R2DBC_URL", url)
                .withSystemProperty("POSTGRES_USER", params.username())
                .withSystemProperty("POSTGRES_PASS", params.password());
    }

    @Test
    void monoSingleSuccess() {
        // given
        var entityCreate = new R2dbcCrudRepository.Entity("1", 1, "2", null);
        repository.insert(entityCreate).block(Duration.ofMinutes(1));

        var foundCreated = repository.findById("1").block(Duration.ofMinutes(1));
        assertNotNull(foundCreated);
        assertEquals("1", foundCreated.id());
        assertEquals(1, foundCreated.field1());
        assertEquals("2", foundCreated.value2());
        assertNull(foundCreated.value3());

        // when
        var entityUpdate = new R2dbcCrudRepository.Entity("1", 5, "6", null);
        repository.update(entityUpdate).block(Duration.ofMinutes(1));

        var foundUpdated = repository.findById("1").block(Duration.ofMinutes(1));
        assertNotNull(foundUpdated);
        assertEquals("1", foundUpdated.id());
        assertEquals(5, foundUpdated.field1());
        assertEquals("6", foundUpdated.value2());
        assertNull(foundUpdated.value3());

        // then
        repository.deleteById("1").block(Duration.ofMinutes(1));
        assertNull(repository.findById("1").block(Duration.ofMinutes(1)));
    }

    @Test
    void monoBatchSuccess() {
        // given
        var entityCreate1 = new R2dbcCrudRepository.Entity("1", 1, "2", null);
        var entityCreate2 = new R2dbcCrudRepository.Entity("2", 3, "4", null);
        assertEquals(2L, repository.insertBatch(List.of(entityCreate1, entityCreate2)).block(Duration.ofMinutes(1)).value());

        var foundCreated = repository.findAllMono().block(Duration.ofMinutes(1));
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
        var entityUpdate1 = new R2dbcCrudRepository.Entity("1", 5, "6", null);
        var entityUpdate2 = new R2dbcCrudRepository.Entity("2", 7, "8", null);
        assertEquals(2L, repository.updateBatch(List.of(entityUpdate1, entityUpdate2)).block(Duration.ofMinutes(1)).value());

        var foundUpdated = repository.findAll().collectList().block(Duration.ofMinutes(1));
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
        assertEquals(2L, repository.deleteAll().block(Duration.ofMinutes(1)).value());
        assertTrue(repository.findAll().collectList().block(Duration.ofMinutes(1)).isEmpty());
    }
}
