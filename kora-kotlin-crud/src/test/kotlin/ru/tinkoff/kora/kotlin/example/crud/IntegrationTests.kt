package ru.tinkoff.kora.kotlin.example.crud

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.Network
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.example.crud.openapi.http.server.model.CategoryCreateTO
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetCreateTO
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetUpdateTO
import ru.tinkoff.kora.kotlin.example.crud.service.PetService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent


/**
 * Тестовый контейнер приложение, который расширяет основное приложение и например добавляет некоторые компоненты
 * из общих модулей, которые не используются в данном приложении, которые могут быть например использованы в других подобных приложениях.
 * Например, когда у вас есть разные приложения READ API и WRITE API.
 * Либо, вам нужны некоторые функции сохранения/удаления/обновления только для тестирования в качестве быстрой тестовой утилиты.
 * <p>
 * Но мы НАСТОЯТЕЛЬНО РЕКОМЕНДУЕМ ИСПОЛЬЗОВАТЬ тестирование приложения как черный ящик в качестве основного инструмента тестирования.
 * -------
 * Test Application than extends Real Application and may be adds some components
 * from common modules that are not used in Real App, but may be used in other similar apps.
 * Like when you have different READ API application and WRITE API application
 * or may be, you need some save/delete/update methods only for testing as fast test utils.
 * <p>
 * But we STRONGLY ENCOURAGE AND RECOMMEND TO USE black box testing as a primary source of truth for tests.
 */
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
class IntegrationTests(@ConnectionPostgreSQL val connection: JdbcConnection) : KoraAppTestConfigModifier {

    @TestComponent
    lateinit var petService: PetService

    @TestComponent
    lateinit var testPetRepository: TestApplication.TestPetRepository

    @TestComponent
    lateinit var testCategoryRepository: TestApplication.TestCategoryRepository

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
        db {
          jdbcUrl = "${connection.params().jdbcUrl()}"
          username = "${connection.params().username()}"
          password = "${connection.params().password()}"
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

    @Test
    fun updatePetWithNewCategoryCreated() {
        // given
        val added = petService.add(PetCreateTO("dog", CategoryCreateTO("dog")))
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)

        // when
        val updated = petService.update(
            added.id,
            PetUpdateTO("cat", PetUpdateTO.StatusEnum.PENDING, CategoryCreateTO("cat"))
        )
        assertNotNull(updated)
        assertEquals(1, updated!!.id)
        assertEquals(2, updated.category.id)

        // then
        assertEquals(1, testPetRepository.findAll().size)
        assertEquals(2, testCategoryRepository.findAll().size)
    }

    @Test
    fun updatePetWithSameCategory() {
        // given
        val added = petService.add(PetCreateTO("dog", CategoryCreateTO("dog")))
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)

        // when
        val updated = petService.update(
            added.id,
            PetUpdateTO("cat", PetUpdateTO.StatusEnum.PENDING, CategoryCreateTO("dog"))
        )
        assertNotNull(updated)
        assertEquals(1, updated!!.id)
        assertEquals(1, updated.category.id)

        // then
        assertEquals(1, testPetRepository.findAll().size)
        assertEquals(1, testCategoryRepository.findAll().size)
    }
}
