package ru.tinkoff.kora.kotlin.example.jdbc

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
class JdbcCrudExtendedCompositeTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: JdbcCrudExtendedCompositeRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun extendedCompositeCrudSuccess() {
        val entity1 =
            JdbcCrudExtendedCompositeRepository.Entity(JdbcCrudExtendedCompositeRepository.Entity.EntityId(), "one")
        val entity2 =
            JdbcCrudExtendedCompositeRepository.Entity(JdbcCrudExtendedCompositeRepository.Entity.EntityId(), "two")
        assertEquals(1L, repository.insert(entity1).value())
        assertTrue(repository.findById(entity1.id).isPresent)
        assertEquals(2L, repository.upsertBatch(listOf(entity1.copy(name = "three"), entity2)).value())
        assertEquals(2, repository.findAll().size)
        assertEquals(1L, repository.deleteById(entity1.id).value())
        assertEquals(1L, repository.deleteBatch(listOf(entity2)).value())
    }
}
