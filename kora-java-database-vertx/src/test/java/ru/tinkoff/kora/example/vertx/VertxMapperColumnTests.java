package ru.tinkoff.kora.example.vertx;

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
class VertxMapperColumnTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private VertxCrudSyncRepository crudRepository;
    @TestComponent
    private VertxMapperColumnRepository mapperRepository;

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
    void columnMapperSuccess() {
        // given
        var entity1 = new VertxCrudSyncRepository.Entity("1", 1, "2", null);
        var entity2 = new VertxCrudSyncRepository.Entity("2", 3, "4", null);

        // when
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).value());

        // then
        var parts = mapperRepository.findAll();
        assertEquals(2, parts.size());
        for (var part : parts) {
            if (part.id().equals("1")) {
                assertEquals("1", part.id());
                Assertions.assertEquals(VertxMapperColumnRepository.Entity.FieldType.ONE, part.field1());
            } else {
                assertEquals("2", part.id());
                Assertions.assertEquals(VertxMapperColumnRepository.Entity.FieldType.UNKNOWN, part.field1());
            }
        }
    }
}
