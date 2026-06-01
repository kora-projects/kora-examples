package ru.tinkoff.kora.example.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
class JdbcMapperParameterTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private JdbcCrudSyncRepository crudRepository;
    @TestComponent
    private JdbcMapperParameterRepository mapperRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void parameterMapperSuccess() {
        // given
        var entity1 = new JdbcCrudSyncRepository.Entity("1", JdbcMapperParameterRepository.Entity.FieldType.ONE.code(), "2",
                null);
        var entity2 = new JdbcCrudSyncRepository.Entity("2", 3, "4", null);
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).value());

        // when
        mapperRepository.updateFieldType(entity1.id(), JdbcMapperParameterRepository.Entity.FieldType.TWO);

        // then
        var found = crudRepository.findById(entity1.id());
        assertNotNull(found);
        assertEquals(JdbcMapperParameterRepository.Entity.FieldType.TWO.code(), found.field1());
    }
}
