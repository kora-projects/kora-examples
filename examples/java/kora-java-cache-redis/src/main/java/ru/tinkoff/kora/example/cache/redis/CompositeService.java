package ru.tinkoff.kora.example.cache.redis;

import jakarta.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import ru.tinkoff.kora.cache.CacheKeyMapper;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.cache.annotation.Cacheable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public class CompositeService {

    public record UserContext(String userId, String traceId) {}

    public static final class UserContextMapping implements CacheKeyMapper<CompositeCache.Key, UserContext> {

        @Nonnull
        @Override
        public CompositeCache.Key map(UserContext arg) {
            return new CompositeCache.Key(arg.userId(), arg.traceId());
        }
    }

    @Mapping(UserContextMapping.class)
    @Cacheable(CompositeCache.class)
    public Long getMapping(UserContext context) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @Cacheable(CompositeCache.class)
    public Long get(String id, String traceId) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CachePut(value = CompositeCache.class, parameters = { "id", "traceId" })
    public Long put(BigDecimal arg2, String arg3, String id, String traceId) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CacheInvalidate(CompositeCache.class)
    public void delete(String id, String traceId) {}

    @CacheInvalidate(value = CompositeCache.class, invalidateAll = true)
    public void deleteAll() {}
}
