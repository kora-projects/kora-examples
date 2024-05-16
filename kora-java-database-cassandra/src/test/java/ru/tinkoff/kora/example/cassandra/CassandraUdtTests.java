package ru.tinkoff.kora.example.cassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.cassandra.CassandraConnection;
import io.goodforgod.testcontainers.extensions.cassandra.ConnectionCassandra;
import io.goodforgod.testcontainers.extensions.cassandra.Migration;
import io.goodforgod.testcontainers.extensions.cassandra.TestcontainersCassandra;
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
                locations = { "migrations" },
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class CassandraUdtTests implements KoraAppTestConfigModifier {

    @ConnectionCassandra
    private CassandraConnection connection;

    @TestComponent
    private CassandraUdtRepository repository;

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
    void monoSingleSuccess() {
        // given
        var entityCreate = new CassandraUdtRepository.Entity("1", new CassandraUdtRepository.Entity.Name("Foo", "Bar"));

        // when
        repository.insert(entityCreate);

        // then
        var foundCreated = repository.findById("1");
        assertNotNull(foundCreated);
        assertEquals("1", foundCreated.id());
        assertEquals(entityCreate.name(), foundCreated.name());
    }
}
