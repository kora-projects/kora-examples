package io.koraframework.example.cache.redis;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import io.koraframework.cache.CacheKeyMapper;
import io.koraframework.cache.annotation.CacheInvalidate;
import io.koraframework.cache.annotation.CacheInvalidateAll;
import io.koraframework.cache.annotation.CachePut;
import io.koraframework.cache.annotation.Cacheable;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.common.annotation.Root;

@Root
@Component
public class CompositeService {

    public record UserContext(String userId, String traceId) {}

    @Component
    public static final class UserContextMapping implements CacheKeyMapper<CompositeCache.Key, UserContext> {

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

    @CachePut(value = CompositeCache.class, args = { "id", "traceId" })
    public Long put(BigDecimal arg2, String arg3, String id, String traceId) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CacheInvalidate(CompositeCache.class)
    public void delete(String id, String traceId) {}

    @CacheInvalidateAll(CompositeCache.class)
    public void deleteAll() {}
}
