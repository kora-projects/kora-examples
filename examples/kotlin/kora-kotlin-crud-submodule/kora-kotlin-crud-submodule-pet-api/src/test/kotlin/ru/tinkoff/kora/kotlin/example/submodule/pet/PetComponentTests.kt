package ru.tinkoff.kora.kotlin.example.submodule.pet

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.Pet
import ru.tinkoff.kora.kotlin.example.submodule.pet.repository.CategoryRepository
import ru.tinkoff.kora.kotlin.example.submodule.pet.repository.PetRepository
import ru.tinkoff.kora.kotlin.example.submodule.pet.service.PetCache
import ru.tinkoff.kora.kotlin.example.submodule.pet.service.PetService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(TestPetApplication::class)
class PetComponentTests : KoraAppTestConfigModifier {

    @Mock
    @TestComponent
    lateinit var petCache: PetCache

    @Mock
    @TestComponent
    lateinit var petRepository: PetRepository

    @Mock
    @TestComponent
    lateinit var categoryRepository: CategoryRepository

    @TestComponent
    lateinit var petService: PetService

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
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
        mockCache()
        mockRepository(mapOf("dog" to 1L, "cat" to 2L))

        val added = petService.add("dog", "dog")
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)

        Mockito.`when`(petRepository.findById(ArgumentMatchers.anyLong())).thenReturn(added)
        val updated = petService.update(added.id, "cat", "cat", Pet.Status.PENDING)
        assertTrue(updated != null)
        assertEquals(1, updated!!.id)
        assertEquals(2, updated.category.id)

        Mockito.verify(petRepository).insert(any())
        Mockito.verify(categoryRepository, Mockito.times(2)).insert(any<String>())
    }

    @Test
    fun updatePetWithSameCategory() {
        mockCache()
        mockRepository(mapOf("dog" to 1L))

        val added = petService.add("dog", "dog")
        assertEquals(1, added.id)
        assertEquals(1, added.category.id)

        Mockito.`when`(petRepository.findById(ArgumentMatchers.anyLong())).thenReturn(added)
        Mockito.`when`(categoryRepository.findByName(any())).thenReturn(added.category)
        val updated = petService.update(added.id, "cat", "cat", Pet.Status.PENDING)
        assertTrue(updated != null)
        assertNotEquals(0, updated!!.id)
        assertNotEquals(0, updated.category.id)

        Mockito.verify(petRepository).insert(any())
        Mockito.verify(categoryRepository).insert(any<String>())
    }

    private fun mockCache() {
        Mockito.`when`(petCache.get(ArgumentMatchers.anyLong())).thenReturn(null)
        Mockito.`when`(petCache.put(ArgumentMatchers.anyLong(), any())).thenAnswer { it.arguments[1] }
        Mockito.`when`(petCache.get(ArgumentMatchers.anyCollection())).thenReturn(emptyMap())
    }

    private fun mockRepository(categoryNameToId: Map<String, Long>) {
        categoryNameToId.forEach { (name, id) -> Mockito.`when`(categoryRepository.insert(name)).thenReturn(id) }
        Mockito.`when`(categoryRepository.findByName(any())).thenReturn(null)
        Mockito.`when`(petRepository.insert(any())).thenReturn(1L)
        Mockito.`when`(petRepository.findById(ArgumentMatchers.anyLong())).thenReturn(null)
    }
}
