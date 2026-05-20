package ru.tinkoff.kora.kotlin.example.cache.redis

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.redis.ConnectionRedis
import io.goodforgod.testcontainers.extensions.redis.RedisConnection
import io.goodforgod.testcontainers.extensions.redis.TestcontainersRedis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.math.BigDecimal

@TestcontainersRedis(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class RedisSimpleServiceTests : KoraAppTestConfigModifier {

    @ConnectionRedis
    lateinit var connection: RedisConnection

    @TestComponent
    lateinit var service: SimpleService

    @TestComponent
    lateinit var cache: SimpleCache

    override fun config(): KoraConfigModification = KoraConfigModification
        .ofSystemProperty("REDIS_URL", connection.params().uri().toString())
        .withSystemProperty("REDIS_USER", connection.params().username())
        .withSystemProperty("REDIS_PASS", connection.params().password())

    @BeforeEach
    fun cleanup() {
        Thread.sleep(150)
        cache.invalidateAll()
    }

    @Test
    fun get() {
        val origin = service.get("1")
        val cached = service.get("1")
        assertEquals(origin, cached)
    }

    @Test
    fun put() {
        val origin = service.put(BigDecimal.ONE, "12345", "1")
        val cached = service.get("1")
        assertEquals(origin, cached)
    }

    @Test
    fun delete() {
        val origin = service.put(BigDecimal.ONE, "12345", "1")
        val cached1 = service.get("1")
        assertEquals(origin, cached1)

        service.delete("1")

        val newValue = service.get("1")
        assertNotEquals(origin, newValue)
    }

    @Test
    fun deleteAll() {
        val origin1 = service.put(BigDecimal.ONE, "12345", "1")
        val cached1 = service.get("1")
        assertEquals(origin1, cached1)

        val origin2 = service.put(BigDecimal.ONE, "12345", "2")
        val cached2 = service.get("2")
        assertEquals(origin2, cached2)

        service.deleteAll()

        val newValue1 = service.get("1")
        assertNotEquals(origin1, newValue1)

        val newValue2 = service.get("2")
        assertNotEquals(origin2, newValue2)
    }
}
