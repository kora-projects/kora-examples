package io.koraframework.kotlin.example.crud

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import io.koraframework.example.crud.openapi.http.server.model.CategoryCreateTO
import io.koraframework.example.crud.openapi.http.server.model.PetCreateTO
import io.koraframework.example.crud.openapi.http.server.model.PetUpdateTO
import io.koraframework.kotlin.example.crud.model.PetWithCategory
import io.koraframework.kotlin.example.crud.repository.CategoryRepository
import io.koraframework.kotlin.example.crud.repository.PetRepository
import io.koraframework.kotlin.example.crud.service.PetCache
import io.koraframework.kotlin.example.crud.service.PetService
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class ComponentTests : KoraAppTestConfigModifier {

    @field:MockK
    @TestComponent
    lateinit var petCache: PetCache

    @field:MockK
    @TestComponent
    lateinit var petRepository: PetRepository

    @field:MockK
    @TestComponent
    lateinit var categoryRepository: CategoryRepository

    @TestComponent
    lateinit var petService: PetService

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
           resilient {
              circuitbreaker.pet {
                type = FIXED_WINDOW
                countBased.windowSize = 2
                minimumRequiredCalls = 2
                failureRateThreshold = 100
                permittedCallsInHalfOpenState = 1
                waitDurationInOpenState = 15s
              }
              timeout.pet {
                duration = 5000ms
              }
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
        mockCache()
        mockRepository(mapOf("dog" to 1, "cat" to 2))

        val added = petService.add(PetCreateTO("dog", CategoryCreateTO("dog")))
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)
        verify { petRepository.insert(any()) }
        verify { categoryRepository.insert(any()) }

        // when
        every { petRepository.findById(any()) } returns added
        val updated = petService.update(
            added.id,
            PetUpdateTO(status = PetUpdateTO.StatusEnum.PENDING, name = "cat", category = CategoryCreateTO("cat"))
        )
        assertNotNull(updated)
        assertEquals(1, updated!!.id)
        assertEquals(2, updated.category.id)

        // then
        verify { petRepository.update(any()) }
        verify { categoryRepository.insert(any()) }
    }

    @Test
    fun updatePetWithSameCategory() {
        // given
        mockCache()
        mockRepository(mapOf("dog" to 1, "cat" to 2))

        val added = petService.add(PetCreateTO("dog", CategoryCreateTO("dog")))
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)
        verify { petRepository.insert(any()) }
        verify { categoryRepository.insert(any()) }

        // when
        every { petRepository.findById(any()) } returns added
        every { categoryRepository.findByName(any()) } returns added.category
        val updated = petService.update(
            added.id,
            PetUpdateTO(status = PetUpdateTO.StatusEnum.PENDING, name = "cat", category = CategoryCreateTO("dog"))
        )
        assertNotNull(updated)
        assertEquals(1, updated!!.id)
        assertEquals(1, updated.category.id)

        // then
        verify { petRepository.update(any()) }
        verify { categoryRepository.insert(any()) }
    }

    private fun mockCache() {
        every { petCache.get(any<Long>()) } returns null
        every { petCache.put(any<Long>(), any()) } returnsArgument (1)
        every { petCache.get(any<Collection<Long>>()) } returns emptyMap<Long, PetWithCategory>()
    }

    private fun mockRepository(categoryNameToId: Map<String, Long>) {
        categoryNameToId.forEach { (name, id) -> every { categoryRepository.insert(name) } returns id }
        every { categoryRepository.findByName(any()) } returns null
        every { petRepository.insert(any()) } returns 1
        every { petRepository.findById(any()) } returns null
        every { petRepository.update(any()) } returns Unit
    }
}
