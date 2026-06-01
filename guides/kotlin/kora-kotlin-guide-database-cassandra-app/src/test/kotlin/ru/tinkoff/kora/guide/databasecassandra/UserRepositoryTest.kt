package ru.tinkoff.kora.guide.databasecassandra

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.scylla.ConnectionScylla
import io.goodforgod.testcontainers.extensions.scylla.Migration
import io.goodforgod.testcontainers.extensions.scylla.ScyllaConnection
import io.goodforgod.testcontainers.extensions.scylla.TestcontainersScylla
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.databasecassandra.repository.UserDAO
import ru.tinkoff.kora.guide.databasecassandra.repository.UserRepository
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Instant

@TestcontainersScylla(
    mode = ContainerMode.PER_RUN,
    migration = Migration(
        engine = Migration.Engines.SCRIPTS,
        locations = ["migrations"],
        apply = Migration.Mode.PER_METHOD,
        drop = Migration.Mode.PER_METHOD
    )
)
@KoraAppTest(Application::class)
class UserRepositoryTest : KoraAppTestConfigModifier {
    @ConnectionScylla
    lateinit var connection: ScyllaConnection

    @TestComponent
    lateinit var userRepository: UserRepository

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("CASSANDRA_CONTACT_POINTS", connection.params().contactPoint())
            .withSystemProperty("CASSANDRA_USER", connection.params().username())
            .withSystemProperty("CASSANDRA_PASS", connection.params().password())
            .withSystemProperty("CASSANDRA_DC", connection.params().datacenter())
            .withSystemProperty("CASSANDRA_KEYSPACE", connection.params().keyspace())

    @Test
    fun repositoryPersistsAndReadsUsers() {
        val createdAt = Instant.parse("2026-05-10T10:15:30Z")
        userRepository.save(UserDAO("user-1", "John Doe", "john@example.com", createdAt))

        val found = userRepository.findById("user-1")

        assertNotNull(found)
        assertEquals("user-1", found!!.id)
        assertEquals("John Doe", found.name)
        assertEquals("john@example.com", found.email)
        assertEquals(createdAt, found.createdAt)
    }

    @Test
    fun repositoryUpdatesAndDeletesUsers() {
        val createdAt = Instant.parse("2026-05-10T10:15:30Z")
        userRepository.save(UserDAO("user-2", "Jane Smith", "jane@example.com", createdAt))

        userRepository.update(UserDAO("user-2", "Jane Updated", "jane.updated@example.com", createdAt))
        val updated = userRepository.findById("user-2")

        assertNotNull(updated)
        assertEquals("Jane Updated", updated!!.name)
        assertEquals("jane.updated@example.com", updated.email)

        userRepository.deleteById("user-2")

        assertNull(userRepository.findById("user-2"))
    }

    @Test
    fun repositoryListsUsers() {
        userRepository.save(UserDAO("user-3", "Alice", "alice@example.com", Instant.parse("2026-05-10T10:15:30Z")))
        userRepository.save(UserDAO("user-4", "Bob", "bob@example.com", Instant.parse("2026-05-10T10:16:30Z")))

        val users = userRepository.findAll()

        assertEquals(2, users.size)
        assertTrue(users.any { it.id == "user-3" })
        assertTrue(users.any { it.id == "user-4" })
    }
}
