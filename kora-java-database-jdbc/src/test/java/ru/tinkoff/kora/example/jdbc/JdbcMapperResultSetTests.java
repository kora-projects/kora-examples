package ru.tinkoff.kora.example.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
class JdbcMapperResultSetTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private JdbcCrudSyncRepository crudRepository;
    @TestComponent
    private JdbcMapperResultSetRepository mapperRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void resultSetMapperSuccess() {
        // given
        var entity1 = new JdbcCrudSyncRepository.Entity("1", 1, "2", null);
        var entity2 = new JdbcCrudSyncRepository.Entity("2", 3, "4", null);

        // when
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).value());

        // then
        var found = mapperRepository.findAllParts();
        var fieldOne = found.get(1);
        assertNotNull(fieldOne);
        assertEquals(1, fieldOne.size());
        assertEquals(entity1.id(), fieldOne.get(0).id());

        var fieldUnknown = found.get(3);
        assertNotNull(fieldUnknown);
        assertEquals(1, fieldUnknown.size());
        assertEquals(entity2.id(), fieldUnknown.get(0).id());
    }
}
