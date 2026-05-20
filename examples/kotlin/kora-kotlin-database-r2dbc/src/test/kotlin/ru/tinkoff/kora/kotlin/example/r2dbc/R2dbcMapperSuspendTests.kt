package ru.tinkoff.kora.kotlin.example.r2dbc

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import kotlinx.coroutines.runBlocking
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
class R2dbcMapperSuspendTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection
    @TestComponent
    lateinit var crudRepository: R2dbcCrudSuspendRepository
    @TestComponent
    lateinit var columnRepository: R2dbcMapperColumnRepository
    @TestComponent
    lateinit var parameterRepository: R2dbcMapperParameterRepository
    @TestComponent
    lateinit var rowRepository: R2dbcMapperRowRepository
    override fun config(): KoraConfigModification = r2dbcConfig(connection)

    @Test
    fun mapperSuccess() = runBlocking {
        crudRepository.insert(R2dbcEntity("1", 1, "2", null))
        crudRepository.insert(R2dbcEntity("2", 3, "4", null))

        assertEquals(
            R2dbcMapperColumnRepository.Entity.FieldType.ONE,
            columnRepository.findAll().first { it.id == "1" }.field1
        )
        parameterRepository.updateFieldType("1", R2dbcMapperParameterRepository.Entity.FieldType.TWO)
        assertEquals(R2dbcMapperParameterRepository.Entity.FieldType.TWO.code, crudRepository.findById("1")?.field1)
        assertEquals(2, rowRepository.findAllParts().size)
    }
}
