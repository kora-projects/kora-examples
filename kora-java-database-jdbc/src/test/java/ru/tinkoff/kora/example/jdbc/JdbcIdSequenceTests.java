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
class JdbcIdSequenceTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
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
    void syncSingleSuccess() {
        // given
        var entityCreate = new JdbcIdSequenceRepository.Entity("Bob");

        // when
        long id = repository.insert(entityCreate);
        assertNotEquals(0, id);

        var ids = repository
                .createGenerated(List.of(new JdbcIdSequenceRepository.Entity("b1"), new JdbcIdSequenceRepository.Entity("b2")));
        for (Long id2 : ids) {
            assertNotEquals(0, id2);
        }

        var id3 = repository.createGenerated(new JdbcIdSequenceRepository.Entity("b3"));
        assertNotEquals(0, id3);

        // then
        var foundCreated = repository.findById(id);
        assertNotNull(foundCreated);
        assertEquals(id, foundCreated.id());
        assertEquals(entityCreate.name(), foundCreated.name());
    }
}
