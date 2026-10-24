package io.koraframework.example.cache.caffeine;

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
public class SimpleService {

    public record UserContext(String userId, String traceId) {}

    @Component
    public static final class UserContextMapping implements CacheKeyMapper<String, UserContext> {

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

    @CachePut(value = SimpleCache.class, args = { "id" })
    public Long put(BigDecimal arg2, String arg3, String id) {
        return ThreadLocalRandom.current().nextLong(0, 100_000_000L);
    }

    @CacheInvalidate(SimpleCache.class)
    public void delete(String id) {}

    @CacheInvalidateAll(SimpleCache.class)
    public void deleteAll() {}
}
