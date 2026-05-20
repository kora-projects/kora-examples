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
class JdbcIdSequenceCompositeTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var repository: JdbcIdSequenceCompositeRepository

    override fun config(): KoraConfigModification = jdbcConfig(connection)

    @Test
    fun syncSingleSuccess() {
        assertEquals(1L, repository.insert(JdbcIdSequenceCompositeRepository.Entity("Ivan")).value())
        val id = repository.insertGenerated(JdbcIdSequenceCompositeRepository.Entity("Petr"))
        assertNotNull(id.a)
        assertNotNull(id.b)
        assertEquals(id, repository.findById(id)?.id)
        assertEquals(
            2,
            repository.insertGenerated(
                listOf(
                    JdbcIdSequenceCompositeRepository.Entity("a"),
                    JdbcIdSequenceCompositeRepository.Entity("b")
                )
            ).size
        )
    }
}
