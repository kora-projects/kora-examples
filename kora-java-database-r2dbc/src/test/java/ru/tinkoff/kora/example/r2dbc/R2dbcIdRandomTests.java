package ru.tinkoff.kora.example.r2dbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ContainerPostgresConnection;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgres;
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
class R2dbcIdRandomTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private R2dbcIdRandomRepository repository;

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
    void syncSingleSuccess() {
        // given
        var entityCreate = new R2dbcIdRandomRepository.Entity("Ivan");

        // when
        repository.insert(entityCreate).block();

        // then
        var foundCreated = repository.findById(entityCreate.id()).block();
        assertNotNull(foundCreated);
        assertEquals(entityCreate.id(), foundCreated.id());
        assertEquals(entityCreate.name(), foundCreated.name());
    }
}
