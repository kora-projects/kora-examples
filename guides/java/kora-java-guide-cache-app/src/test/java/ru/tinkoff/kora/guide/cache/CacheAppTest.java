package ru.tinkoff.kora.guide.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.cache.dto.UserRequest;
import ru.tinkoff.kora.guide.cache.repository.InMemoryUserRepository;
import ru.tinkoff.kora.guide.cache.service.UserCaffeineCache;
import ru.tinkoff.kora.guide.cache.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class CacheAppTest {

    @TestComponent
    private UserService userService;
    @TestComponent
    private UserCaffeineCache userCache;
    @TestComponent
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void cleanup() {
        this.userCache.invalidateAll();
        this.userRepository.resetStats();
    }

    @Test
    void cacheComponentsAreWired() {
        assertNotNull(this.userService);
        assertNotNull(this.userCache);
        assertNotNull(this.userRepository);
    }

    @Test
    void createUserManuallyPutsCreatedUserIntoCache() {
        var created = this.userService.createUser(new UserRequest("Bob", "bob@example.com"));

        assertEquals(created, this.userCache.get(created.id()));

        this.userRepository.resetStats();
        var cached = this.userService.getUser(created.id());

        assertTrue(cached.isPresent());
        assertEquals(created, cached.orElseThrow());
        assertEquals(0, this.userRepository.getFindByIdCalls());
    }

    @Test
    void cacheablePopulatesCacheOnFirstReadAndUsesCacheOnSecondRead() {
        var created = this.userService.createUser(new UserRequest("Bob", "bob@example.com"));
        this.userCache.invalidate(created.id());
        this.userRepository.resetStats();

        assertNull(this.userCache.get(created.id()));

        var first = this.userService.getUser(created.id());

        assertTrue(first.isPresent());
        assertEquals(created.id(), first.orElseThrow().id());
        assertEquals(created.id(), this.userCache.get(created.id()).id());
        assertEquals(1, this.userRepository.getFindByIdCalls());

        var second = this.userService.getUser(created.id());

        assertTrue(second.isPresent());
        assertEquals(created.id(), second.orElseThrow().id());
        assertEquals(1, this.userRepository.getFindByIdCalls());
    }

    @Test
    void cachePutRefreshesCachedValueOnUpdate() {
        var created = this.userService.createUser(new UserRequest("Alice", "alice@example.com"));
        this.userRepository.resetStats();

        var updated = this.userService.updateUser(created.id(), new UserRequest("Alice Updated", "alice.updated@example.com"));

        assertEquals(updated, this.userCache.get(created.id()));

        var cached = this.userService.getUser(created.id());

        assertTrue(cached.isPresent());
        assertEquals(updated, cached.orElseThrow());
        assertEquals(0, this.userRepository.getFindByIdCalls());
    }

    @Test
    void cacheInvalidateRemovesCachedValueAfterDelete() {
        var created = this.userService.createUser(new UserRequest("Carol", "carol@example.com"));
        assertEquals(created, this.userCache.get(created.id()));

        this.userService.deleteUser(created.id());

        assertNull(this.userCache.get(created.id()));

        this.userRepository.resetStats();
        var afterDelete = this.userService.getUser(created.id());

        assertTrue(afterDelete.isEmpty());
        assertEquals(1, this.userRepository.getFindByIdCalls());
    }
}
