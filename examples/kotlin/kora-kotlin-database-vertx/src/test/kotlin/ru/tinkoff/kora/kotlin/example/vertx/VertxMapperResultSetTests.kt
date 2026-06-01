package ru.tinkoff.kora.kotlin.example.vertx

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
class VertxMapperResultSetTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var crudRepository: VertxCrudSyncRepository

    @TestComponent
    lateinit var mapperRepository: VertxMapperRowSetRepository

    override fun config(): KoraConfigModification = vertxConfig(connection)

    @Test
    fun resultSetMapperSuccess() {
        val entity1 = VertxEntity("1", 1, "2", null)
        val entity2 = VertxEntity("2", 3, "4", null)

        assertEquals(2, crudRepository.insertBatch(listOf(entity1, entity2)).value())

        val found = mapperRepository.findAllParts()
        val fieldOne = found[1]
        assertNotNull(fieldOne)
        assertEquals(1, fieldOne?.size)
        assertEquals(entity1.id, fieldOne?.get(0)?.id)

        val fieldUnknown = found[3]
        assertNotNull(fieldUnknown)
        assertEquals(1, fieldUnknown?.size)
        assertEquals(entity2.id, fieldUnknown?.get(0)?.id)
    }
}
