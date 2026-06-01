package ru.tinkoff.kora.kotlin.example.jdbc

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.assertEquals
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
class JdbcMapperParameterTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var crudRepository: JdbcCrudSyncRepository

    @TestComponent
    lateinit var mapperRepository: JdbcMapperParameterRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun parameterMapperSuccess() {
        crudRepository.insert(JdbcEntity("1", JdbcMapperParameterRepository.Entity.FieldType.ONE.code, "2", null))
        mapperRepository.updateFieldType("1", JdbcMapperParameterRepository.Entity.FieldType.TWO)
        assertEquals(JdbcMapperParameterRepository.Entity.FieldType.TWO.code, crudRepository.findById("1")?.field1)
    }
}
