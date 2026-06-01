package ru.tinkoff.kora.guide.databasejdbc.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL;
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection;
import io.goodforgod.testcontainers.extensions.jdbc.Migration;
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.databasejdbc.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.databasejdbc.advanced.service.UserService;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskRequest;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.service.TaskService;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
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
class TaskServicePostgresAdvancedTest implements KoraAppTestConfigModifier {

    @ConnectionPostgreSQL
    private JdbcConnection connection;

    @TestComponent
    private UserService userService;

    @TestComponent
    private TaskService taskService;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                db {
                  jdbcUrl = ${POSTGRES_JDBC_URL}
                  username = ${POSTGRES_USER}
                  password = ${POSTGRES_PASS}
                  poolName = "kora-jdbc-advanced-test"
                }
                flyway {
                  locations = "db/migration"
                }
                """)
                .withSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
                .withSystemProperty("POSTGRES_USER", connection.params().username())
                .withSystemProperty("POSTGRES_PASS", connection.params().password());
    }

    @Test
    void createTasks_ShouldUseBatchInsertAndReturnCreatedTasks() {
        var created = taskService.createTasks(List.of(
                new TaskRequest.TaskCreate("Write guide", "Draft advanced JDBC guide", null),
                new TaskRequest.TaskCreate("Review SQL", null, null)));

        assertEquals(2, created.size());
        assertEquals("Write guide", created.get(0).title());
        assertEquals(TaskStatus.TODO, created.get(0).status());
        assertEquals("Review SQL", created.get(1).title());
    }

    @Test
    void assignTask_ShouldUpdateAssignee() {
        var user = userService.createUser(new UserRequest("Alice", "alice@example.com"));
        var task = taskService.createTasks(List.of(new TaskRequest.TaskCreate("Review SQL", null, null))).get(0);

        taskService.assignTask(task.id(), Long.parseLong(user.id()));
        var assigned = taskService.getTasksByAssignees(List.of(Long.parseLong(user.id())));

        assertEquals(1, assigned.size());
        assertEquals(task.id(), assigned.get(0).id());
        assertEquals("Alice", assigned.get(0).assignee().name());
    }

    @Test
    void createTasksWithMissingAssignee_ShouldRollbackAllTasks() {
        var user = userService.createUser(new UserRequest("Alice", "alice@example.com"));

        assertThrows(HttpServerResponseException.class, () -> taskService.createTasks(List.of(
                new TaskRequest.TaskCreate("Valid task", null, Long.parseLong(user.id())),
                new TaskRequest.TaskCreate("Invalid task", null, 999999L))));

        assertTrue(taskService.getTasksByAssignees(List.of(Long.parseLong(user.id()))).isEmpty());
    }

    @Test
    void findByAssigneeIds_ShouldUsePostgresArrayParameterMapper() {
        var alice = userService.createUser(new UserRequest("Alice", "alice@example.com"));
        var bob = userService.createUser(new UserRequest("Bob", "bob@example.com"));
        var carol = userService.createUser(new UserRequest("Carol", "carol@example.com"));

        taskService.createTasks(List.of(
                new TaskRequest.TaskCreate("Alice task", null, Long.parseLong(alice.id())),
                new TaskRequest.TaskCreate("Bob task", null, Long.parseLong(bob.id())),
                new TaskRequest.TaskCreate("Carol task", null, Long.parseLong(carol.id()))));

        var result = taskService.getTasksByAssignees(List.of(Long.parseLong(alice.id()), Long.parseLong(bob.id())));

        assertEquals(2, result.size());
    }
}
