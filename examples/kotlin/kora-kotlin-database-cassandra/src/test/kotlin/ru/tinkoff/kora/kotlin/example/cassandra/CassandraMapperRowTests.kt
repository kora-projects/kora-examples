package ru.tinkoff.kora.kotlin.example.cassandra

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.scylla.ConnectionScylla
import io.goodforgod.testcontainers.extensions.scylla.Migration
import io.goodforgod.testcontainers.extensions.scylla.ScyllaConnection
import io.goodforgod.testcontainers.extensions.scylla.TestcontainersScylla
import org.junit.jupiter.api.Assertions.assertEquals
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
class CassandraMapperRowTests : KoraAppTestConfigModifier {
    @ConnectionScylla
    lateinit var connection: ScyllaConnection

    @TestComponent
    lateinit var crudRepository: CassandraCrudSyncRepository

    @TestComponent
    lateinit var mapperRepository: CassandraMapperRowRepository

    override fun config(): KoraConfigModification = cassandraConfig(connection)

    @Test
    fun rowMapperSuccess() {
        crudRepository.insertBatch(listOf(CassandraEntity("1", 1, "2", null), CassandraEntity("2", 3, "4", null)))
        assertEquals(2, mapperRepository.findAllParts().size)
    }
}
