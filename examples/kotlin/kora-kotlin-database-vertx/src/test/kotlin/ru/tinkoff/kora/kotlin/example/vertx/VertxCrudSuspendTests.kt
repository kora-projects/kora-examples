package ru.tinkoff.kora.kotlin.example.vertx

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
class VertxCrudSuspendTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection
    @TestComponent
    lateinit var repository: VertxCrudSuspendRepository
    override fun config(): KoraConfigModification = vertxConfig(connection)

    @Test
    fun suspendSingleSuccess() = runBlocking {
        repository.insert(VertxEntity("1", 1, "2", null))
        assertEquals(1, repository.findById("1")?.field1)
        repository.update(VertxEntity("1", 5, "6", null))
        assertEquals("6", repository.findById("1")?.value2)
        repository.deleteById("1")
        assertNull(repository.findById("1"))
    }
}
