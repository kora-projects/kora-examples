package ru.tinkoff.kora.guide.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.redis.ConnectionRedis;
import io.goodforgod.testcontainers.extensions.redis.RedisConnection;
import io.goodforgod.testcontainers.extensions.redis.TestcontainersRedis;
import java.time.temporal.ChronoUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.cache.dto.UserRequest;
import ru.tinkoff.kora.guide.cache.dto.UserResponse;
import ru.tinkoff.kora.guide.cache.repository.InMemoryUserRepository;
import ru.tinkoff.kora.guide.cache.service.UserCaffeineCache;
import ru.tinkoff.kora.guide.cache.service.UserRedisCache;
import ru.tinkoff.kora.guide.cache.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersRedis(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class CacheAppTest implements KoraAppTestConfigModifier {

    @ConnectionRedis
    private RedisConnection connection;

    @TestComponent
    private UserService userService;
    @TestComponent
    private UserCaffeineCache userCaffeineCache;
    @TestComponent
    private UserRedisCache userRedisCache;
    @TestComponent
    private InMemoryUserRepository userRepository;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("REDIS_URL", connection.params().uri().toString());
    }

    @BeforeEach
    void cleanup() throws Exception {
        Thread.sleep(150);
        this.userCaffeineCache.invalidateAll();
        this.userRedisCache.invalidateAll();
        this.userRepository.resetStats();
    }

    @Test
    void createUserManuallyWarmsBothCacheLevels() {
        var created = this.userService.createUser(new UserRequest("Bob", "bob@example.com"));

        assertUserMatches(created, this.userCaffeineCache.get(created.id()));
        assertUserMatches(created, this.userRedisCache.get(created.id()));

        this.userRepository.resetStats();
        var cached = this.userService.getUser(created.id());

        assertTrue(cached.isPresent());
        assertUserMatches(created, cached.orElseThrow());
        assertEquals(0, this.userRepository.getFindByIdCalls());
    }

    @Test
    void redisActsAsSecondLevelCacheWhenCaffeineIsCold() {
        var created = this.userService.createUser(new UserRequest("Alice", "alice@example.com"));
        this.userCaffeineCache.invalidate(created.id());
        this.userRepository.resetStats();

        assertNull(this.userCaffeineCache.get(created.id()));
        assertUserMatches(created, this.userRedisCache.get(created.id()));

        var fetched = this.userService.getUser(created.id());

        assertTrue(fetched.isPresent());
        assertUserMatches(created, fetched.orElseThrow());
        assertEquals(0, this.userRepository.getFindByIdCalls());
        assertUserMatches(created, this.userCaffeineCache.get(created.id()));
    }

    @Test
    void cachePutRefreshesBothCacheLevelsOnUpdate() {
        var created = this.userService.createUser(new UserRequest("Carol", "carol@example.com"));

        var updated = this.userService.updateUser(created.id(), new UserRequest("Carol Updated", "carol.updated@example.com"));

        assertUserMatches(updated, this.userCaffeineCache.get(created.id()));
        assertUserMatches(updated, this.userRedisCache.get(created.id()));

        this.userRepository.resetStats();
        var cached = this.userService.getUser(created.id());

        assertTrue(cached.isPresent());
        assertUserMatches(updated, cached.orElseThrow());
        assertEquals(0, this.userRepository.getFindByIdCalls());
    }

    @Test
    void cacheInvalidateRemovesValuesFromBothCacheLevels() {
        var created = this.userService.createUser(new UserRequest("Dave", "dave@example.com"));
        assertUserMatches(created, this.userCaffeineCache.get(created.id()));
        assertUserMatches(created, this.userRedisCache.get(created.id()));

        this.userService.deleteUser(created.id());

        assertNull(this.userCaffeineCache.get(created.id()));
        assertNull(this.userRedisCache.get(created.id()));

        this.userRepository.resetStats();
        var afterDelete = this.userService.getUser(created.id());

        assertTrue(afterDelete.isEmpty());
        assertEquals(1, this.userRepository.getFindByIdCalls());
    }

    private static void assertUserMatches(UserResponse expected, UserResponse actual) {
        assertNotNull(actual);
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.email(), actual.email());
        assertEquals(expected.createdAt().truncatedTo(ChronoUnit.MILLIS), actual.createdAt().truncatedTo(ChronoUnit.MILLIS));
    }
}

