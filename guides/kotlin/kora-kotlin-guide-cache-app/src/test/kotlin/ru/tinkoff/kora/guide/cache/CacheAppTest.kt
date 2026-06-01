package ru.tinkoff.kora.guide.cache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.cache.dto.UserRequest
import ru.tinkoff.kora.guide.cache.service.UserCaffeineCache
import ru.tinkoff.kora.guide.cache.service.UserService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class CacheAppTest {
    @TestComponent
    lateinit var userService: UserService

    @TestComponent
    lateinit var userCache: UserCaffeineCache

    @Test
    fun cacheComponentsAreWired() {
        assertNotNull(userService)
        assertNotNull(userCache)
    }

    @Test
    fun createUserManuallyPutsCreatedUserIntoCache() {
        val created = userService.createUser(UserRequest("Bob", "bob@example.com"))

        assertEquals(created, userCache.get(created.id))
        assertEquals(created, userService.getUser(created.id))
    }

    @Test
    fun cacheablePopulatesCacheOnFirstRead() {
        val created = userService.createUser(UserRequest("Alice", "alice@example.com"))
        userCache.invalidate(created.id)

        assertNull(userCache.get(created.id))

        val first = userService.getUser(created.id)

        assertNotNull(first)
        assertEquals(created.id, first!!.id)
        assertEquals(created.id, userCache.get(created.id).id)
    }

    @Test
    fun cachePutRefreshesCachedValueOnUpdate() {
        val created = userService.createUser(UserRequest("Carol", "carol@example.com"))

        val updated = userService.updateUser(created.id, UserRequest("Carol Updated", "carol.updated@example.com"))

        assertEquals(updated, userCache.get(created.id))
        assertEquals(updated, userService.getUser(created.id))
    }

    @Test
    fun cacheInvalidateRemovesCachedValueAfterDelete() {
        val created = userService.createUser(UserRequest("Dave", "dave@example.com"))
        assertEquals(created, userCache.get(created.id))

        userService.deleteUser(created.id)

        assertNull(userCache.get(created.id))
        assertNull(userService.getUser(created.id))
    }
}
