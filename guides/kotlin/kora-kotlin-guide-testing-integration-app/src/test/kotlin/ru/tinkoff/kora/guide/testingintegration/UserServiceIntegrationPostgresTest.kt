package ru.tinkoff.kora.guide.testingintegration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.tinkoff.kora.guide.databasejdbc.dto.UserRequest
import ru.tinkoff.kora.guide.databasejdbc.service.UserService
import ru.tinkoff.kora.guide.testingintegration.TestApplication.TestUserRepository
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Duration

@Testcontainers
@KoraAppTest(TestApplication::class)
class UserServiceIntegrationPostgresTest : KoraAppTestConfigModifier {

    companion object {
        @Container
        @JvmStatic
        val POSTGRES = PostgreSQLContainer("postgres:16-alpine")
            .withStartupTimeout(Duration.ofSeconds(30))
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(PostgreSQLContainer::class.java)))
    }

    @TestComponent
    lateinit var userService: UserService

    @TestComponent
    lateinit var testUserRepository: TestUserRepository

    override fun config(): KoraConfigModification {
        return KoraConfigModification.ofString(
            """
            db {
              jdbcUrl = ${'$'}{POSTGRES_JDBC_URL}
              username = ${'$'}{POSTGRES_USER}
              password = ${'$'}{POSTGRES_PASS}
              poolName = "kora-test"
            }
            flyway {
              locations = "db/migration"
            }
            """.trimIndent()
        )
            .withSystemProperty("POSTGRES_JDBC_URL", POSTGRES.jdbcUrl)
            .withSystemProperty("POSTGRES_USER", POSTGRES.username)
            .withSystemProperty("POSTGRES_PASS", POSTGRES.password)
    }

    @BeforeEach
    fun cleanup() {
        testUserRepository.deleteAll()
    }

    @Test
    fun createUserShouldPersistUserInDatabase() {
        val result = userService.createUser(UserRequest("John", "john@example.com"))

        assertEquals("John", result.name)
        assertTrue(result.id.toLong() > 0)
        assertEquals(1, testUserRepository.findAll().size)
    }

    @Test
    fun getUsersWithPaginationShouldReturnCorrectPage() {
        listOf(
            UserRequest("Alice", "alice@example.com"),
            UserRequest("Bob", "bob@example.com"),
            UserRequest("Charlie", "charlie@example.com"),
            UserRequest("David", "david@example.com")
        ).forEach(userService::createUser)

        val result = userService.getUsers(1, 2, "name")

        assertEquals(2, result.size)
        assertEquals("Charlie", result[0].name)
        assertEquals("David", result[1].name)
    }

    @Test
    fun updateUserShouldUpdateUserInDatabase() {
        val created = userService.createUser(UserRequest("John", "john@example.com"))

        val updated = userService.updateUser(created.id, UserRequest("John Updated", "john.updated@example.com"))

        assertEquals("John Updated", updated.name)
    }

    @Test
    fun deleteUserShouldRemoveUserFromDatabase() {
        val created = userService.createUser(UserRequest("John", "john@example.com"))

        userService.deleteUser(created.id)

        assertEquals(0, testUserRepository.findAll().size)
    }
}
