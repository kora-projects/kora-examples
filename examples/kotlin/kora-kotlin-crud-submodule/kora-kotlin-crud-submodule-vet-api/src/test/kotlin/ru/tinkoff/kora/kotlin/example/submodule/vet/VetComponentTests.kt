package ru.tinkoff.kora.kotlin.example.submodule.vet

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import ru.tinkoff.kora.kotlin.example.submodule.vet.repository.VetRepository
import ru.tinkoff.kora.kotlin.example.submodule.vet.service.VetCache
import ru.tinkoff.kora.kotlin.example.submodule.vet.service.VetService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(TestVetApplication::class)
class VetComponentTests : KoraAppTestConfigModifier {

    @Mock
    @TestComponent
    lateinit var vetCache: VetCache

    @Mock
    @TestComponent
    lateinit var vetRepository: VetRepository

    @TestComponent
    lateinit var vetService: VetService

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
        resilient {
           circuitbreaker.vet {
             slidingWindowSize = 2
             minimumRequiredCalls = 2
             failureRateThreshold = 100
             permittedCallsInHalfOpenState = 1
             waitDurationInOpenState = 15s
           }
           timeout.vet.duration = 5000ms
           retry.vet {
             delay = 100ms
             attempts = 0
           }
         }
        """.trimIndent()
    )

    @Test
    fun updateVetWithNewCategoryCreated() {
        mockCache()
        mockRepository()

        val added = vetService.add("dog", "dog")
        assertEquals(1, added.id)

        Mockito.`when`(vetRepository.findById(ArgumentMatchers.anyLong())).thenReturn(added)
        val updated = vetService.update(added.id, "cat", "cat")
        assertTrue(updated != null)
        assertEquals(1, updated!!.id)

        Mockito.verify(vetRepository).insert(any())
    }

    @Test
    fun updateVetWithSameCategory() {
        mockCache()
        mockRepository()

        val added = vetService.add("dog", "dog")
        assertEquals(1, added.id)

        Mockito.`when`(vetRepository.findById(ArgumentMatchers.anyLong())).thenReturn(added)
        val updated = vetService.update(added.id, "cat", "cat")
        assertTrue(updated != null)
        assertNotEquals(0, updated!!.id)

        Mockito.verify(vetRepository).insert(any())
    }

    private fun mockCache() {
        Mockito.`when`(vetCache.get(ArgumentMatchers.anyLong())).thenReturn(null)
        Mockito.`when`(vetCache.put(ArgumentMatchers.anyLong(), any())).thenAnswer { it.arguments[1] }
        Mockito.`when`(vetCache.get(ArgumentMatchers.anyCollection())).thenReturn(emptyMap())
    }

    private fun mockRepository() {
        Mockito.`when`(vetRepository.insert(any())).thenReturn(1L)
        Mockito.`when`(vetRepository.findById(ArgumentMatchers.anyLong())).thenReturn(null)
    }
}
