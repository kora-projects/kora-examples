package ru.tinkoff.kora.example.r2dbc;

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
class R2dbcIdSequenceTests implements KoraAppTestConfigModifier {

    @ContainerPostgresConnection
    private JdbcConnection connection;

    @TestComponent
    private R2dbcIdSequenceRepository repository;

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
        var entityCreate = new R2dbcIdSequenceRepository.Entity("Bob");

        // when
        long id = repository.insert(entityCreate).block();
        assertNotEquals(0, id);

        var ids = repository
                .createGenerated(List.of(new R2dbcIdSequenceRepository.Entity("b1"), new R2dbcIdSequenceRepository.Entity("b2")))
                .block();
        for (Long id2 : ids) {
            assertNotEquals(0, id2);
        }

        var id3 = repository.createGenerated(new R2dbcIdSequenceRepository.Entity("b3")).block();
        assertNotEquals(0, id3);

        // then
        var foundCreated = repository.findById(id).block();
        assertNotNull(foundCreated);
        assertEquals(id, foundCreated.id());
        assertEquals(entityCreate.name(), foundCreated.name());
    }
}
