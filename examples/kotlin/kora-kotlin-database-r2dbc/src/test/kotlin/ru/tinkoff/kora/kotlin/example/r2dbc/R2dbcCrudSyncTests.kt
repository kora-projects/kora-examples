package ru.tinkoff.kora.kotlin.example.r2dbc

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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
class R2dbcCrudSyncTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: R2dbcCrudSyncRepository

    override fun config(): KoraConfigModification = r2dbcConfig(connection)

    @Test
    fun monoSingleSuccess() {
        repository.insert(R2dbcEntity("1", 1, "2", null))
        val foundCreated = repository.findById("1")
        assertNotNull(foundCreated)
        assertEquals(1, foundCreated?.field1)

        repository.update(R2dbcEntity("1", 5, "6", null))
        val foundUpdated = repository.findById("1")
        assertNotNull(foundUpdated)
        assertEquals(5, foundUpdated?.field1)
        assertEquals("6", foundUpdated?.value2)

        repository.deleteById("1")
        assertNull(repository.findById("1"))
    }

    @Test
    fun monoBatchSuccess() {
        assertEquals(
            2L,
            repository.insertBatch(listOf(R2dbcEntity("1", 1, "2", null), R2dbcEntity("2", 3, "4", null))).value()
        )
        assertEquals(2, repository.findAll().size)

        assertEquals(
            2L,
            repository.updateBatch(listOf(R2dbcEntity("1", 5, "6", null), R2dbcEntity("2", 7, "8", null))).value()
        )
        val foundUpdated = repository.findAll()
        assertTrue(foundUpdated.any { it.id == "1" && it.field1 == 5 })
        assertTrue(foundUpdated.any { it.id == "2" && it.field1 == 7 })

        assertEquals(2L, repository.deleteAll().value())
        assertTrue(repository.findAll().isEmpty())
    }
}
