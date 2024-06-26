package ru.tinkoff.kora.example.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
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
class JdbcMapperColumnTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private JdbcCrudSyncRepository crudRepository;
    @TestComponent
    private JdbcMapperColumnRepository mapperRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void columnMapperSuccess() {
        // given
        var entity1 = new JdbcCrudSyncRepository.Entity("1", 1, "2", null);
        var entity2 = new JdbcCrudSyncRepository.Entity("2", 3, "4", null);

        // when
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).value());

        // then
        var parts = mapperRepository.findAll();
        assertEquals(2, parts.size());
        for (var part : parts) {
            if (part.id().equals("1")) {
                assertEquals("1", part.id());
                Assertions.assertEquals(JdbcMapperColumnRepository.Entity.FieldType.ONE, part.field1());
            } else {
                assertEquals("2", part.id());
                Assertions.assertEquals(JdbcMapperColumnRepository.Entity.FieldType.UNKNOWN, part.field1());
            }
        }
    }
}
