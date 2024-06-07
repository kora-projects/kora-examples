package ru.tinkoff.kora.example.cache.redis;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.cache.annotation.Cacheable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public class SimpleService {

    @Cacheable(SimpleCache.class)
    public Long get(String id) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CachePut(value = SimpleCache.class, parameters = { "id" })
    public Long put(BigDecimal arg2, String arg3, String id) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CacheInvalidate(SimpleCache.class)
    public void delete(String id) {}

    @CacheInvalidate(value = SimpleCache.class, invalidateAll = true)
    public void deleteAll() {}
}
