package io.koraframework.example.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.*;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.example.jdbc.JdbcIdRandomCompositeRepository.Entity;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

@TestcontainersPostgreSQL(
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                engine = Migration.Engines.FLYWAY,
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class JdbcIdRandomCompositeTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private JdbcIdRandomCompositeRepository repository;

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

        var foundCreated = repository.findById(entityCreate.id());
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
        var countMany = repository.insert(entities);
        assertEquals(2, countMany.value());

        var foundCreated = repository.findById(entities.get(0).id());
        assertNotNull(foundCreated);
        assertEquals(entities.get(0).name(), foundCreated.name());
    }
}
