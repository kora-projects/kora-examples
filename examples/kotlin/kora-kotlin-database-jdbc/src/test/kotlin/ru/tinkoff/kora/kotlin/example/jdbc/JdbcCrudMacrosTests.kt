package ru.tinkoff.kora.kotlin.example.jdbc

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
class JdbcCrudMacrosTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: JdbcCrudMacrosRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun monoSingleSuccess() {
        assertEquals(1L, repository.insert(JdbcMacrosEntity("1", 1, "2", null)).value())
        assertEquals(1, repository.findById("1")?.field1)
        assertEquals(1L, repository.update(JdbcMacrosEntity("1", 5, "6", null)).value())
        assertEquals("6", repository.findById("1")?.value2)
        assertEquals(1L, repository.deleteById("1").value())
        assertNull(repository.findById("1"))
    }

    @Test
    fun batchAndUpsertSuccess() {
        assertEquals(
            2L,
            repository.insertBatch(listOf(JdbcMacrosEntity("1", 1, "2", null), JdbcMacrosEntity("2", 3, "4", null)))
                .value()
        )
        assertEquals(
            2L,
            repository.updateBatch(listOf(JdbcMacrosEntity("1", 5, "6", null), JdbcMacrosEntity("2", 7, "8", null)))
                .value()
        )
        assertEquals(1L, repository.upsert(JdbcMacrosEntity("1", 9, "10", null)).value())
        assertEquals(
            2L,
            repository.upsertBatch(listOf(JdbcMacrosEntity("1", 11, "12", null), JdbcMacrosEntity("2", 13, "14", null)))
                .value()
        )
        assertEquals(2, repository.findAll().size)
    }
}
