package ru.tinkoff.kora.guide.testingintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.guide.databasejdbc.dto.UserRequest;
import ru.tinkoff.kora.guide.databasejdbc.service.UserService;
import ru.tinkoff.kora.guide.testingintegration.TestApplication.TestUserRepository;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@Testcontainers
@KoraAppTest(TestApplication.class)
class UserServiceIntegrationPostgresTest implements KoraAppTestConfigModifier {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withStartupTimeout(Duration.ofSeconds(30))
                    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(PostgreSQLContainer.class)));

    @TestComponent
    private UserService userService;

    @TestComponent
    private TestUserRepository testUserRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                db {
                  jdbcUrl = ${POSTGRES_JDBC_URL}
                  username = ${POSTGRES_USER}
                  password = ${POSTGRES_PASS}
                  poolName = "kora-test"
                }
                flyway {
                  locations = "db/migration"
                }
                """)
                .withSystemProperty("POSTGRES_JDBC_URL", POSTGRES.getJdbcUrl())
                .withSystemProperty("POSTGRES_USER", POSTGRES.getUsername())
                .withSystemProperty("POSTGRES_PASS", POSTGRES.getPassword());
    }

    @BeforeEach
    void cleanup() {
        testUserRepository.deleteAll();
    }

    @Test
    void createUser_ShouldPersistUserInDatabase() {
        var result = userService.createUser(new UserRequest("John", "john@example.com"));

        assertEquals("John", result.name());
        assertTrue(Long.parseLong(result.id()) > 0);
        assertEquals(1, testUserRepository.findAll().size());
    }

    @Test
    void getUsers_WithPagination_ShouldReturnCorrectPage() {
        List.of(
                        new UserRequest("Alice", "alice@example.com"),
                        new UserRequest("Bob", "bob@example.com"),
                        new UserRequest("Charlie", "charlie@example.com"),
                        new UserRequest("David", "david@example.com"))
                .forEach(userService::createUser);

        var result = userService.getUsers(1, 2, "name");

        assertEquals(2, result.size());
        assertEquals("Charlie", result.get(0).name());
        assertEquals("David", result.get(1).name());
    }

    @Test
    void updateUser_ShouldUpdateUserInDatabase() {
        var created = userService.createUser(new UserRequest("John", "john@example.com"));

        var updated = userService.updateUser(created.id(), new UserRequest("John Updated", "john.updated@example.com"));

        assertEquals("John Updated", updated.name());
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        var created = userService.createUser(new UserRequest("John", "john@example.com"));

        userService.deleteUser(created.id());

        assertEquals(0, testUserRepository.findAll().size());
    }
}
