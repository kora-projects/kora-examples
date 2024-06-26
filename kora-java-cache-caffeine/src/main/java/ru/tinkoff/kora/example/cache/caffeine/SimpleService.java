package ru.tinkoff.kora.example.cache.caffeine;

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
public class SimpleService {

    public record UserContext(String userId, String traceId) {}

    public static final class UserContextMapping implements CacheKeyMapper<String, UserContext> {

        @Nonnull
        @Override
        public String map(UserContext arg) {
            return arg.userId();
        }
    }

    @Mapping(UserContextMapping.class)
    @Cacheable(SimpleCache.class)
    public Long getMapping(UserContext context) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

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
