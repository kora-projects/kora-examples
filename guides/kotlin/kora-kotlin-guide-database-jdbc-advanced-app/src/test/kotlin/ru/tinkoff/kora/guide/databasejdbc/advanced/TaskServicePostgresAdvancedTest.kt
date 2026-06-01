package ru.tinkoff.kora.guide.databasejdbc.advanced

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.databasejdbc.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.databasejdbc.advanced.service.UserService
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskRequest
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.service.TaskService
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersPostgreSQL(
    mode = ContainerMode.PER_RUN,
    migration = Migration(
        engine = Migration.Engines.FLYWAY,
        apply = Migration.Mode.PER_METHOD,
        drop = Migration.Mode.PER_METHOD
    )
)
@KoraAppTest(Application::class)
class TaskServicePostgresAdvancedTest : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var userService: UserService

    @TestComponent
    lateinit var taskService: TaskService

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofString(
            """
            db {
              jdbcUrl = ${'$'}{POSTGRES_JDBC_URL}
              username = ${'$'}{POSTGRES_USER}
              password = ${'$'}{POSTGRES_PASS}
              poolName = "kora-jdbc-advanced-test"
            }
            flyway {
              locations = "db/migration"
            }
            """.trimIndent()
        )
            .withSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
            .withSystemProperty("POSTGRES_USER", connection.params().username())
            .withSystemProperty("POSTGRES_PASS", connection.params().password())

    @Test
    fun createTasksShouldUseBatchInsertAndReturnCreatedTasks() {
        val created = taskService.createTasks(
            listOf(
                TaskRequest.TaskCreate("Write guide", "Draft advanced JDBC guide", null),
                TaskRequest.TaskCreate("Review SQL", null, null)
            )
        )

        assertEquals(2, created.size)
        assertEquals("Write guide", created[0].title)
        assertEquals(TaskStatus.TODO, created[0].status)
        assertEquals("Review SQL", created[1].title)
    }

    @Test
    fun assignTaskShouldUpdateAssignee() {
        val user = userService.createUser(UserRequest("Alice", "alice@example.com"))
        val task = taskService.createTasks(listOf(TaskRequest.TaskCreate("Review SQL", null, null)))[0]

        taskService.assignTask(task.id, user.id.toLong())
        val assigned = taskService.getTasksByAssignees(listOf(user.id.toLong()))

        assertEquals(1, assigned.size)
        assertEquals(task.id, assigned[0].id)
        assertEquals("Alice", assigned[0].assignee.name)
    }

    @Test
    fun createTasksWithMissingAssigneeShouldRollbackAllTasks() {
        val user = userService.createUser(UserRequest("Alice", "alice@example.com"))

        assertThrows(HttpServerResponseException::class.java) {
            taskService.createTasks(
                listOf(
                    TaskRequest.TaskCreate("Valid task", null, user.id.toLong()),
                    TaskRequest.TaskCreate("Invalid task", null, 999999L)
                )
            )
        }

        assertTrue(taskService.getTasksByAssignees(listOf(user.id.toLong())).isEmpty())
    }

    @Test
    fun findByAssigneeIdsShouldUsePostgresArrayParameterMapper() {
        val alice = userService.createUser(UserRequest("Alice", "alice@example.com"))
        val bob = userService.createUser(UserRequest("Bob", "bob@example.com"))
        val carol = userService.createUser(UserRequest("Carol", "carol@example.com"))

        taskService.createTasks(
            listOf(
                TaskRequest.TaskCreate("Alice task", null, alice.id.toLong()),
                TaskRequest.TaskCreate("Bob task", null, bob.id.toLong()),
                TaskRequest.TaskCreate("Carol task", null, carol.id.toLong())
            )
        )

        val result = taskService.getTasksByAssignees(listOf(alice.id.toLong(), bob.id.toLong()))

        assertEquals(2, result.size)
    }
}
