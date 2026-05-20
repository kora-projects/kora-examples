package ru.tinkoff.kora.kotlin.example.jdbc

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class JdbcCrudExtendedTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: JdbcCrudExtendedRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun extendedCrudSuccess() {
        val entity1 = JdbcCrudExtendedRepository.Entity("1", 1, "2", null)
        val entity2 = JdbcCrudExtendedRepository.Entity("2", 3, "4", null)
        assertEquals(1L, repository.insert(entity1).value())
        assertNotNull(repository.findById("1"))
        assertEquals(1L, repository.update(JdbcCrudExtendedRepository.Entity("1", 5, "6", null)).value())
        assertEquals(
            2L,
            repository.upsertBatch(listOf(JdbcCrudExtendedRepository.Entity("1", 7, "8", null), entity2)).value()
        )
        assertEquals(2, repository.findAll().size)
        assertEquals(1L, repository.delete(entity2).value())
        assertEquals(1L, repository.deleteById("1").value())
    }
}
