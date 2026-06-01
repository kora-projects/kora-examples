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
class VertxMapperParameterTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var crudRepository: VertxCrudSyncRepository

    @TestComponent
    lateinit var mapperRepository: VertxMapperParameterRepository

    override fun config(): KoraConfigModification = vertxConfig(connection)

    @Test
    fun parameterMapperSuccess() {
        val entity1 = VertxEntity("1", VertxMapperParameterRepository.Entity.FieldType.ONE.code, "2", null)
        val entity2 = VertxEntity("2", 3, "4", null)
        assertEquals(2, crudRepository.insertBatch(listOf(entity1, entity2)).value())

        mapperRepository.updateFieldType(entity1.id, VertxMapperParameterRepository.Entity.FieldType.TWO)

        val found = crudRepository.findById(entity1.id)
        assertNotNull(found)
        assertEquals(VertxMapperParameterRepository.Entity.FieldType.TWO.code, found?.field1)
    }
}
