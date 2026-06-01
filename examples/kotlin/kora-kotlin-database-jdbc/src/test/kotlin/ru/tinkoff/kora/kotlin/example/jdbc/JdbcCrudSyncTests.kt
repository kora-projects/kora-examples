package ru.tinkoff.kora.kotlin.example.jdbc

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
class JdbcCrudSyncTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: JdbcCrudSyncRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun monoSingleSuccess() {
        repository.insert(JdbcEntity("1", 1, "2", null))
        assertEquals(1, repository.findById("1")?.field1)
        repository.update(JdbcEntity("1", 5, "6", null))
        assertEquals("6", repository.findById("1")?.value2)
        repository.deleteById("1")
        assertNull(repository.findById("1"))
    }

    @Test
    fun monoBatchSuccess() {
        assertEquals(
            2L,
            repository.insertBatch(listOf(JdbcEntity("1", 1, "2", null), JdbcEntity("2", 3, "4", null))).value()
        )
        assertEquals(2, repository.findAll().size)
        assertEquals(
            2L,
            repository.updateBatch(listOf(JdbcEntity("1", 5, "6", null), JdbcEntity("2", 7, "8", null))).value()
        )
        assertTrue(repository.findAll().any { it.id == "2" && it.field1 == 7 })
        assertEquals(2L, repository.deleteAll().value())
    }

    @Test
    fun mappedArrayParameterSuccess() {
        repository.insertBatch(listOf(JdbcEntity("1", 1, "2", null), JdbcEntity("2", 3, "4", null)))
        assertEquals(2, repository.findAllByIds(listOf("1", "2")).size)
    }
}
