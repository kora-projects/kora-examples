package ru.tinkoff.kora.kotlin.example.jdbc

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
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
class JdbcIdSequenceTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: JdbcIdSequenceRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun syncSingleSuccess() {
        val id = repository.insert(JdbcIdSequenceRepository.Entity("Ivan"))
        assertNotEquals(0, id)
        assertEquals(
            2,
            repository.insertGenerated(
                listOf(
                    JdbcIdSequenceRepository.Entity("b1"),
                    JdbcIdSequenceRepository.Entity("b2")
                )
            ).size
        )
        assertNotEquals(0, repository.insertGenerated(JdbcIdSequenceRepository.Entity("b3")))
        assertEquals("Ivan", repository.findById(id)?.name)
    }
}
