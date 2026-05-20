package ru.tinkoff.kora.kotlin.example.cache.caffeine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.math.BigDecimal

@KoraAppTest(Application::class)
class CaffeineCompositeServiceTests {

    @TestComponent
    lateinit var service: CompositeService

    @TestComponent
    lateinit var cache: CompositeCache

    @BeforeEach
    fun cleanup() {
        cache.invalidateAll()
    }

    @Test
    fun get() {
        // when
        val origin = service.get("1", "1")

        // then
        val cached = service.get("1", "1")
        assertEquals(origin, cached)
    }

    @Test
    fun getMapping() {
        // given
        val context = CompositeService.UserContext("1", "2")

        // when
        val origin = service.getMapping(context)

        // then
        val cached = service.getMapping(context)
        assertEquals(origin, cached)
    }

    @Test
    fun put() {
        // when
        val origin = service.put(BigDecimal.ONE, "12345", "1", "1")

        // then
        val cached = service.get("1", "1")
        assertEquals(origin, cached)
    }

    @Test
    fun delete() {
        // given
        val origin = service.put(BigDecimal.ONE, "12345", "1", "1")
        val cached1 = service.get("1", "1")
        assertEquals(origin, cached1)

        // when
        service.delete("1", "1")

        // then
        val newValue = service.get("1", "1")
        assertNotEquals(origin, newValue)
    }

    @Test
    fun deleteAll() {
        // given
        val origin1 = service.put(BigDecimal.ONE, "12345", "1", "1")
        val cached1 = service.get("1", "1")
        assertEquals(origin1, cached1)

        val origin2 = service.put(BigDecimal.ONE, "12345", "2", "2")
        val cached2 = service.get("2", "2")
        assertEquals(origin2, cached2)

        // when
        service.deleteAll()

        // then
        val newValue1 = service.get("1", "1")
        assertNotEquals(origin1, newValue1)

        val newValue2 = service.get("2", "2")
        assertNotEquals(origin2, newValue2)
    }
}
