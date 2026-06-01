package ru.tinkoff.kora.guide.databasecassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.scylla.ConnectionScylla;
import io.goodforgod.testcontainers.extensions.scylla.Migration;
import io.goodforgod.testcontainers.extensions.scylla.ScyllaConnection;
import io.goodforgod.testcontainers.extensions.scylla.TestcontainersScylla;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.databasecassandra.repository.UserDAO;
import ru.tinkoff.kora.guide.databasecassandra.repository.UserRepository;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersScylla(
        mode = ContainerMode.PER_RUN,
        migration = @Migration(
                engine = Migration.Engines.SCRIPTS,
                locations = { "migrations" },
                apply = Migration.Mode.PER_METHOD,
                drop = Migration.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class UserRepositoryTest implements KoraAppTestConfigModifier {

    @ConnectionScylla
    private ScyllaConnection connection;

    @TestComponent
    private UserRepository userRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("CASSANDRA_CONTACT_POINTS", connection.params().contactPoint())
                .withSystemProperty("CASSANDRA_USER", connection.params().username())
                .withSystemProperty("CASSANDRA_PASS", connection.params().password())
                .withSystemProperty("CASSANDRA_DC", connection.params().datacenter())
                .withSystemProperty("CASSANDRA_KEYSPACE", connection.params().keyspace());
    }

    @Test
    void repositoryPersistsAndReadsUsers() {
        var createdAt = Instant.parse("2026-05-10T10:15:30Z");
        userRepository.save(new UserDAO("user-1", "John Doe", "john@example.com", createdAt));

        var found = userRepository.findById("user-1");

        assertNotNull(found);
        assertEquals("user-1", found.id());
        assertEquals("John Doe", found.name());
        assertEquals("john@example.com", found.email());
        assertEquals(createdAt, found.createdAt());
    }

    @Test
    void repositoryUpdatesAndDeletesUsers() {
        var createdAt = Instant.parse("2026-05-10T10:15:30Z");
        userRepository.save(new UserDAO("user-2", "Jane Smith", "jane@example.com", createdAt));

        userRepository.update(new UserDAO("user-2", "Jane Updated", "jane.updated@example.com", createdAt));
        var updated = userRepository.findById("user-2");

        assertNotNull(updated);
        assertEquals("Jane Updated", updated.name());
        assertEquals("jane.updated@example.com", updated.email());

        userRepository.deleteById("user-2");

        assertNull(userRepository.findById("user-2"));
    }

    @Test
    void repositoryListsUsers() {
        userRepository.save(new UserDAO("user-3", "Alice", "alice@example.com", Instant.parse("2026-05-10T10:15:30Z")));
        userRepository.save(new UserDAO("user-4", "Bob", "bob@example.com", Instant.parse("2026-05-10T10:16:30Z")));

        var users = userRepository.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(user -> user.id().equals("user-3")));
        assertTrue(users.stream().anyMatch(user -> user.id().equals("user-4")));
    }
}
