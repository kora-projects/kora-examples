package ru.tinkoff.kora.example.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ContainerPostgresConnection;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgres;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.example.jdbc.JdbcIdSequenceCompositeRepository.Entity;
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
class JdbcIdSequenceCompositeTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private JdbcIdSequenceCompositeRepository repository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void insertOne() {
        // given
        var entityCreate = new Entity("Bob");

        // when
        UpdateCount count = repository.insert(entityCreate);
        assertEquals(1, count.value());

        // then
        var foundCreated = repository.findById(new Entity.EntityId(1L, 1L));
        assertNotNull(foundCreated);
        assertEquals(entityCreate.name(), foundCreated.name());
    }

    @Test
    void insertMany() {
        // given
        var entities = List.of(
                new Entity("b1"),
                new Entity("b2"));

        // when
        var ids = repository.insertGenerated(entities);

        for (Entity.EntityId id : ids) {
            assertNotEquals(0, id.a());
            assertNotEquals(0, id.b());
        }

        // then
        var foundCreated = repository.findById(ids.get(0));
        assertNotNull(foundCreated);
        assertEquals(entities.get(0).name(), foundCreated.name());
    }
}
