package ru.tinkoff.kora.guide.cache

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.redis.ConnectionRedis
import io.goodforgod.testcontainers.extensions.redis.RedisConnection
import io.goodforgod.testcontainers.extensions.redis.TestcontainersRedis
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.cache.dto.UserRequest
import ru.tinkoff.kora.guide.cache.dto.UserResponse
import ru.tinkoff.kora.guide.cache.service.UserCaffeineCache
import ru.tinkoff.kora.guide.cache.service.UserRedisCache
import ru.tinkoff.kora.guide.cache.service.UserService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.temporal.ChronoUnit

@TestcontainersRedis(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class CacheAppTest : KoraAppTestConfigModifier {
    @ConnectionRedis
    lateinit var connection: RedisConnection

    @TestComponent
    lateinit var userService: UserService

    @TestComponent
    lateinit var userCaffeineCache: UserCaffeineCache

    @TestComponent
    lateinit var userRedisCache: UserRedisCache

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("REDIS_URL", connection.params().uri().toString())

    @BeforeEach
    fun cleanup() {
        Thread.sleep(150)
        userCaffeineCache.invalidateAll()
        userRedisCache.invalidateAll()
    }

    @Test
    fun createUserManuallyWarmsBothCacheLevels() {
        val created = userService.createUser(UserRequest("Bob", "bob@example.com"))

        assertUserMatches(created, userCaffeineCache.get(created.id))
        assertUserMatches(created, userRedisCache.get(created.id))
        assertUserMatches(created, userService.getUser(created.id))
    }

    @Test
    fun redisActsAsSecondLevelCacheWhenCaffeineIsCold() {
        val created = userService.createUser(UserRequest("Alice", "alice@example.com"))
        userCaffeineCache.invalidate(created.id)

        assertNull(userCaffeineCache.get(created.id))
        assertUserMatches(created, userRedisCache.get(created.id))

        val fetched = userService.getUser(created.id)

        assertUserMatches(created, fetched)
        assertUserMatches(created, userCaffeineCache.get(created.id))
    }

    @Test
    fun cachePutRefreshesBothCacheLevelsOnUpdate() {
        val created = userService.createUser(UserRequest("Carol", "carol@example.com"))

        val updated = userService.updateUser(created.id, UserRequest("Carol Updated", "carol.updated@example.com"))

        assertUserMatches(updated, userCaffeineCache.get(created.id))
        assertUserMatches(updated, userRedisCache.get(created.id))
        assertUserMatches(updated, userService.getUser(created.id))
    }

    @Test
    fun cacheInvalidateRemovesValuesFromBothCacheLevels() {
        val created = userService.createUser(UserRequest("Dave", "dave@example.com"))

        userService.deleteUser(created.id)

        assertNull(userCaffeineCache.get(created.id))
        assertNull(userRedisCache.get(created.id))
        assertNull(userService.getUser(created.id))
    }

    private fun assertUserMatches(expected: UserResponse, actual: UserResponse?) {
        assertNotNull(actual)
        assertEquals(expected.id, actual!!.id)
        assertEquals(expected.name, actual.name)
        assertEquals(expected.email, actual.email)
        assertEquals(
            expected.createdAt.truncatedTo(ChronoUnit.MILLIS),
            actual.createdAt.truncatedTo(ChronoUnit.MILLIS)
        )
    }
}
