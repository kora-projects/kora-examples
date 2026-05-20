package ru.tinkoff.kora.kotlin.example.cassandra

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.scylla.ConnectionScylla
import io.goodforgod.testcontainers.extensions.scylla.Migration
import io.goodforgod.testcontainers.extensions.scylla.ScyllaConnection
import io.goodforgod.testcontainers.extensions.scylla.TestcontainersScylla
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

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
class CassandraCrudSuspendTests : KoraAppTestConfigModifier {
    @ConnectionScylla
    lateinit var connection: ScyllaConnection

    @TestComponent
    lateinit var repository: CassandraCrudSuspendRepository

    override fun config(): KoraConfigModification = cassandraConfig(connection)

    @Test
    fun suspendSingleSuccess() = runBlocking {
        repository.insert(CassandraEntity("1", 1, "2", null))
        assertEquals(1, repository.findById("1")?.field1)
        repository.update(CassandraEntity("1", 5, "6", null))
        assertEquals("6", repository.findById("1")?.value2)
        repository.deleteById("1")
        assertNull(repository.findById("1"))
    }
}
