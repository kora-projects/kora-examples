package ru.tinkoff.kora.example.cache.caffeine;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.cache.annotation.Cacheable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public class CachedService {

    @Cacheable(MyCache.class)
    public Long get(String id) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CachePut(value = MyCache.class, parameters = { "id" })
    public Long put(BigDecimal arg2, String arg3, String id) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CacheInvalidate(MyCache.class)
    public void delete(String id) {}

    @CacheInvalidate(value = MyCache.class, invalidateAll = true)
    public void deleteAll() {}
}
