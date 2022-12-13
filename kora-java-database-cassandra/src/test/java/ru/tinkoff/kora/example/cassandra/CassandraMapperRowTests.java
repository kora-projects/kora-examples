package ru.tinkoff.kora.example.cassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.cassandra.CassandraConnection;
import io.goodforgod.testcontainers.extensions.cassandra.ContainerCassandraConnection;
import io.goodforgod.testcontainers.extensions.cassandra.Migration;
import io.goodforgod.testcontainers.extensions.cassandra.TestcontainersCassandra;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersCassandra(
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                engine = Migration.Engines.SCRIPTS,
                migrations = { "migrations" },
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class CassandraMapperRowTests implements KoraAppTestConfigModifier {

    @ContainerCassandraConnection
    private CassandraConnection connection;

    @TestComponent
    private CassandraCrudSyncRepository crudRepository;
    @TestComponent
    private CassandraMapperRowRepository mapperRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("CASSANDRA_CONTACT_POINTS", connection.params().host() + ":" + connection.params().port())
                .withSystemProperty("CASSANDRA_USER", connection.params().username())
                .withSystemProperty("CASSANDRA_PASS", connection.params().password())
                .withSystemProperty("CASSANDRA_DC", connection.params().datacenter())
                .withSystemProperty("CASSANDRA_KEYSPACE", "cassandra");
    }

    @Test
    void rowMapperSuccess() {
        // given
        var entity1 = new CassandraCrudSyncRepository.Entity("1", 1, "2", null);
        var entity2 = new CassandraCrudSyncRepository.Entity("2", 3, "4", null);

        // when
        crudRepository.insertBatch(List.of(entity1, entity2));

        // then
        var found = mapperRepository.findAllParts();
        assertEquals(2, found.size());
    }
}
