package ru.tinkoff.kora.example.cache.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.redis.ConnectionRedis;
import io.goodforgod.testcontainers.extensions.redis.RedisConnection;
import io.goodforgod.testcontainers.extensions.redis.TestcontainersRedis;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersRedis(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class RedisSimpleServiceTests implements KoraAppTestConfigModifier {

    @ConnectionRedis
    private RedisConnection connection;

    @TestComponent
    private SimpleService service;
    @TestComponent
    private SimpleCache cache;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("REDIS_URL", connection.params().uri().toString())
                .withSystemProperty("REDIS_USER", connection.params().username())
                .withSystemProperty("REDIS_PASS", connection.params().password());
    }

    @BeforeEach
    void cleanup() throws Exception {
        Thread.sleep(150);
        cache.invalidateAll();
    }

    @Test
    void get() {
        // when
        var origin = service.get("1");

        // then
        var cached = service.get("1");
        assertEquals(origin, cached);
    }

    @Test
    void put() {
        // when
        var origin = service.put(BigDecimal.ONE, "12345", "1");

        // then
        var cached = service.get("1");
        assertEquals(origin, cached);
    }

    @Test
    void delete() {
        // given
        var origin = service.put(BigDecimal.ONE, "12345", "1");
        var cached1 = service.get("1");
        assertEquals(origin, cached1);

        // when
        service.delete("1");

        // then
        var newValue = service.get("1");
        assertNotEquals(origin, newValue);
    }

    @Test
    void deleteAll() {
        // given
        var origin1 = service.put(BigDecimal.ONE, "12345", "1");
        var cached1 = service.get("1");
        assertEquals(origin1, cached1);

        var origin2 = service.put(BigDecimal.ONE, "12345", "2");
        var cached2 = service.get("2");
        assertEquals(origin2, cached2);

        // when
        service.deleteAll();

        // then
        var newValue1 = service.get("1");
        assertNotEquals(origin1, newValue1);

        var newValue2 = service.get("2");
        assertNotEquals(origin2, newValue2);
    }
}
