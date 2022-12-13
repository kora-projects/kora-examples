package ru.tinkoff.kora.example.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
class VertxMapperRowTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private VertxCrudSyncRepository crudRepository;
    @TestComponent
    private VertxMapperRowRepository mapperRepository;

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
    void rowMapperSuccess() {
        // given
        var entity1 = new VertxCrudSyncRepository.Entity("1", 1, "2", null);
        var entity2 = new VertxCrudSyncRepository.Entity("2", 3, "4", null);

        // when
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).value());

        // then
        var found = mapperRepository.findAllParts();
        assertEquals(2, found.size());
    }
}
