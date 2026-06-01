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
class JdbcMapperRowTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var crudRepository: JdbcCrudSyncRepository

    @TestComponent
    lateinit var mapperRepository: JdbcMapperRowRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun rowMapperSuccess() {
        crudRepository.insertBatch(listOf(JdbcEntity("1", 1, "2", null), JdbcEntity("2", 3, "4", null)))
        assertEquals(2, mapperRepository.findAllParts().size)
    }
}
