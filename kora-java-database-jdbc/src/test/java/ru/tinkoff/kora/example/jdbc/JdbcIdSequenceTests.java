package ru.tinkoff.kora.example.jdbc;

import static org.junit.jupiter.api.Assertions.*;
import static ru.tinkoff.kora.example.jdbc.JdbcIdSequenceRepository.*;

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
class JdbcIdSequenceTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private JdbcIdSequenceRepository repository;

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
        var entity1 = new Entity("Foo");
        var entity2 = new Entity("Bar");

        // when
        long id1 = repository.insert(entity1);
        assertEquals(1, id1);
        long id2 = repository.insertGenerated(entity2);
        assertEquals(2, id2);

        // then
        var foundCreated = repository.findById(1L);
        assertNotNull(foundCreated);
        assertEquals(entity1.name(), foundCreated.name());
    }

    @Test
    void insertMany() {
        // given
        var entities = List.of(new Entity("b1"), new Entity("b2"));

        // when
        var ids = repository.insertGenerated(entities);
        for (long id : ids) {
            assertNotEquals(0, id);
            assertNotEquals(0, id);
        }

        // then
        var foundCreated = repository.findById(ids.get(0));
        assertNotNull(foundCreated);
        assertEquals(entities.get(0).name(), foundCreated.name());
    }
}
