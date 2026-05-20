package ru.tinkoff.kora.example.cassandra;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.scylla.ConnectionScylla;
import io.goodforgod.testcontainers.extensions.scylla.Migration;
import io.goodforgod.testcontainers.extensions.scylla.ScyllaConnection;
import io.goodforgod.testcontainers.extensions.scylla.TestcontainersScylla;
import java.time.Duration;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersScylla(
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                engine = Migration.Engines.SCRIPTS,
                locations = { "migrations" },
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class CassandraCrudReactorTests implements KoraAppTestConfigModifier {

    @ConnectionScylla
    private ScyllaConnection connection;

    @TestComponent
    private CassandraCrudReactorRepository repository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("CASSANDRA_CONTACT_POINTS", connection.params().contactPoint())
                .withSystemProperty("CASSANDRA_USER", connection.params().username())
                .withSystemProperty("CASSANDRA_PASS", connection.params().password())
                .withSystemProperty("CASSANDRA_DC", connection.params().datacenter())
                .withSystemProperty("CASSANDRA_KEYSPACE", connection.params().keyspace());
    }

    @Test
    void monoSingleSuccess() {
        // given
        var entityCreate = new CassandraCrudReactorRepository.Entity("1", 1, "2", null);
        repository.insert(entityCreate).block(Duration.ofSeconds(15));

        var foundCreated = repository.findById("1").block(Duration.ofSeconds(15));
        assertNotNull(foundCreated);
        assertEquals("1", foundCreated.id());
        assertEquals(1, foundCreated.field1());
        assertEquals("2", foundCreated.value2());
        assertNull(foundCreated.value3());

        // when
        var entityUpdate = new CassandraCrudReactorRepository.Entity("1", 5, "6", null);
        repository.update(entityUpdate).block(Duration.ofSeconds(15));

        var foundUpdated = repository.findById("1").block(Duration.ofSeconds(15));
        assertNotNull(foundUpdated);
        assertEquals("1", foundUpdated.id());
        assertEquals(5, foundUpdated.field1());
        assertEquals("6", foundUpdated.value2());
        assertNull(foundUpdated.value3());

        // then
        repository.deleteById("1").block(Duration.ofSeconds(15));
        assertNull(repository.findById("1").block(Duration.ofSeconds(15)));
    }

    @Test
    void monoBatchSuccess() {
        // given
        var entityCreate1 = new CassandraCrudReactorRepository.Entity("1", 1, "2", null);
        var entityCreate2 = new CassandraCrudReactorRepository.Entity("2", 3, "4", null);
        repository.insertBatch(List.of(entityCreate1, entityCreate2)).block(Duration.ofSeconds(15));

        var foundCreated = repository.findAllMono().block(Duration.ofSeconds(15));
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
        var entityUpdate1 = new CassandraCrudReactorRepository.Entity("1", 5, "6", null);
        var entityUpdate2 = new CassandraCrudReactorRepository.Entity("2", 7, "8", null);
        repository.updateBatch(List.of(entityUpdate1, entityUpdate2)).block(Duration.ofSeconds(15));

        var foundUpdated = repository.findAll().collectList().block(Duration.ofSeconds(15));
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
        repository.deleteAll().block(Duration.ofSeconds(15));
        assertTrue(repository.findAllMono().block(Duration.ofSeconds(15)).isEmpty());
    }
}
