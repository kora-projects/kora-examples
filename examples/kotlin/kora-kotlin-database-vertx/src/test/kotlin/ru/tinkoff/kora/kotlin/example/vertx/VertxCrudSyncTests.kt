package ru.tinkoff.kora.kotlin.example.vertx

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
class VertxCrudSyncTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: VertxCrudSyncRepository

    override fun config(): KoraConfigModification = vertxConfig(connection)

    @Test
    fun singleSuccess() {
        val entityCreate = VertxEntity("1", 1, "2", null)
        repository.insert(entityCreate)

        val foundCreated = repository.findById("1")
        assertNotNull(foundCreated)
        assertEquals("1", foundCreated?.id)
        assertEquals(1, foundCreated?.field1)
        assertEquals("2", foundCreated?.value2)
        assertNull(foundCreated?.value3)

        val entityUpdate = VertxEntity("1", 5, "6", null)
        repository.update(entityUpdate)

        val foundUpdated = repository.findById("1")
        assertNotNull(foundUpdated)
        assertEquals("1", foundUpdated?.id)
        assertEquals(5, foundUpdated?.field1)
        assertEquals("6", foundUpdated?.value2)
        assertNull(foundUpdated?.value3)

        repository.deleteById("1")
        assertNull(repository.findById("1"))
    }

    @Test
    fun batchSuccess() {
        val entity1 = VertxEntity("1", 1, "2", null)
        val entity2 = VertxEntity("2", 3, "4", null)
        assertEquals(2L, repository.insertBatch(listOf(entity1, entity2)).value())

        val foundCreated = repository.findAll()
        assertEquals(2, foundCreated.size)
        for (entity in foundCreated) {
            if (entity.id == "1") {
                assertEquals("1", entity.id)
                assertEquals(1, entity.field1)
                assertEquals("2", entity.value2)
                assertNull(entity.value3)
            } else {
                assertEquals("2", entity.id)
                assertEquals(3, entity.field1)
                assertEquals("4", entity.value2)
                assertNull(entity.value3)
            }
        }

        val entityUpdate1 = VertxEntity("1", 5, "6", null)
        val entityUpdate2 = VertxEntity("2", 7, "8", null)
        assertEquals(2L, repository.updateBatch(listOf(entityUpdate1, entityUpdate2)).value())

        val foundUpdated = repository.findAll()
        assertEquals(2, foundUpdated.size)
        for (entity in foundUpdated) {
            if (entity.id == "1") {
                assertEquals("1", entity.id)
                assertEquals(5, entity.field1)
                assertEquals("6", entity.value2)
                assertNull(entity.value3)
            } else {
                assertEquals("2", entity.id)
                assertEquals(7, entity.field1)
                assertEquals("8", entity.value2)
                assertNull(entity.value3)
            }
        }

        assertEquals(2L, repository.deleteAll().value())
        assertTrue(repository.findAll().isEmpty())
    }
}
