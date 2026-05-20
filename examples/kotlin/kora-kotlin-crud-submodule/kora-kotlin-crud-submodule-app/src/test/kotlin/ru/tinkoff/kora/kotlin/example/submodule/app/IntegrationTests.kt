package ru.tinkoff.kora.kotlin.example.submodule.app

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.Network
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.kotlin.example.submodule.app.TestApplication.TestCategoryRepository
import ru.tinkoff.kora.kotlin.example.submodule.app.TestApplication.TestPetRepository
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.Pet
import ru.tinkoff.kora.kotlin.example.submodule.pet.service.PetService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersPostgreSQL(
    network = Network(shared = true),
    mode = ContainerMode.PER_RUN,
    migration = Migration(
        engine = Migration.Engines.FLYWAY,
        apply = Migration.Mode.PER_METHOD,
        drop = Migration.Mode.PER_METHOD
    )
)
@KoraAppTest(TestApplication::class)
class IntegrationTests : KoraAppTestConfigModifier {
    @ConnectionPostgreSQL
    lateinit var connection: JdbcConnection

    @TestComponent
    lateinit var petService: PetService

    @TestComponent
    lateinit var testPetRepository: TestPetRepository

    @TestComponent
    lateinit var testCategoryRepository: TestCategoryRepository

    override fun config(): KoraConfigModification {
        return KoraConfigModification.ofString(
            """
            db {
              jdbcUrl = ${'$'}{POSTGRES_JDBC_URL}
              username = ${'$'}{POSTGRES_USER}
              password = ${'$'}{POSTGRES_PASS}
              poolName = "kora"
            }
            pet-cache.maximumSize = 0
            resilient {
               circuitbreaker.pet {
                 slidingWindowSize = 2
                 minimumRequiredCalls = 2
                 failureRateThreshold = 100
                 permittedCallsInHalfOpenState = 1
                 waitDurationInOpenState = 15s
               }
               timeout.pet.duration = 5000ms
               retry.pet {
                 delay = 100ms
                 attempts = 0
               }
             }
            """.trimIndent()
        )
            .withSystemProperty("POSTGRES_JDBC_URL", connection.params().jdbcUrl())
            .withSystemProperty("POSTGRES_USER", connection.params().username())
            .withSystemProperty("POSTGRES_PASS", connection.params().password())
    }

    @Test
    fun updatePetWithNewCategoryCreated() {
        val added = petService.add("dog", "dog")
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)

        val updated = petService.update(added.id, "cat", "cat", Pet.Status.PENDING)
        assertNotNull(updated)
        assertEquals(1, updated?.id)
        assertEquals(2, updated?.category?.id)

        assertEquals(1, testPetRepository.findAll().size)
        assertEquals(2, testCategoryRepository.findAll().size)
    }

    @Test
    fun updatePetWithSameCategory() {
        val added = petService.add("dog", "dog")
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)

        val updated = petService.update(added.id, "cat", "dog", Pet.Status.PENDING)
        assertNotNull(updated)
        assertNotEquals(0, updated?.id)
        assertNotEquals(0, updated?.category?.id)

        assertEquals(1, testPetRepository.findAll().size)
        assertEquals(1, testCategoryRepository.findAll().size)
    }
}
