package ru.tinkoff.kora.example.r2dbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
class R2dbcMapperRowTests implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private R2dbcCrudRepository crudRepository;
    @TestComponent
    private R2dbcMapperRowRepository mapperRepository;

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
    void rowMapperSuccess() {
        // given
        var entity1 = new R2dbcCrudRepository.Entity("1", 1, "2", null);
        var entity2 = new R2dbcCrudRepository.Entity("2", 3, "4", null);

        // when
        assertEquals(2, crudRepository.insertBatch(List.of(entity1, entity2)).block().value());

        // then
        var found = mapperRepository.findAllParts().collectList().block();
        assertEquals(2, found.size());
    }
}
