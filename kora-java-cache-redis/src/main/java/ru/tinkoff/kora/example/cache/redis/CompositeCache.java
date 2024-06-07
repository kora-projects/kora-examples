package ru.tinkoff.kora.example.cache.redis;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.redis.RedisCache;

@Cache("my-cache")
public interface CompositeCache extends RedisCache<CompositeCache.Key, Long> {

    record Key(String userId, String traceId) { }
}
