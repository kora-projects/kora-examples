package ru.tinkoff.kora.example.r2dbc;

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
class R2dbcMapperParameterTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private R2dbcCrudRepository crudRepository;
    @TestComponent
    private R2dbcMapperParameterRepository mapperRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        var params = connection.params();
        var url = "r2dbc:postgresql://%s:%s/%s".formatted(params.host(), params.port(), params.database());
        return KoraConfigModification.ofSystemProperty("POSTGRES_R2DBC_URL", url)
                .withSystemProperty("POSTGRES_USER", params.username())
                .withSystemProperty("POSTGRES_PASS", params.password());
    }

    @Test
    void parameterMapperSuccess() {
        // given
        var entity1 = new R2dbcCrudRepository.Entity("1", R2dbcMapperParameterRepository.Entity.FieldType.ONE.code(), "2", null);
        var entity2 = new R2dbcCrudRepository.Entity("2", 3, "4", null);
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).block().value());

        // when
        mapperRepository.updateFieldType(entity1.id(), R2dbcMapperParameterRepository.Entity.FieldType.TWO).block();

        // then
        var found = crudRepository.findById(entity1.id()).block();
        assertNotNull(found);
        assertEquals(R2dbcMapperParameterRepository.Entity.FieldType.TWO.code(), found.field1());
    }
}
