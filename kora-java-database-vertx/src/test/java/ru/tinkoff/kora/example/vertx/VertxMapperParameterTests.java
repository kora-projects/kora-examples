package ru.tinkoff.kora.example.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ContainerPostgresConnection;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgres;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
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
class VertxMapperParameterTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private VertxCrudSyncRepository crudRepository;
    @TestComponent
    private VertxMapperParameterRepository mapperRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        var params = connection.params();
        return KoraConfigModification
                .ofSystemProperty("POSTGRES_VERTX_URI",
                        "postgresql://%s:%s/%s".formatted(params.host(), params.port(), params.database()))
                .withSystemProperty("POSTGRES_USER", params.username())
                .withSystemProperty("POSTGRES_PASS", params.password());
    }

    @Test
    void parameterMapperSuccess() {
        // given
        var entity1 = new VertxCrudSyncRepository.Entity("1", VertxMapperParameterRepository.Entity.FieldType.ONE.code(), "2",
                null);
        var entity2 = new VertxCrudSyncRepository.Entity("2", 3, "4", null);
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).value());

        // when
        mapperRepository.updateFieldType(entity1.id(), VertxMapperParameterRepository.Entity.FieldType.TWO);

        // then
        var found = crudRepository.findById(entity1.id());
        assertNotNull(found);
        Assertions.assertEquals(VertxMapperParameterRepository.Entity.FieldType.TWO.code(), found.field1());
    }
}
