package io.koraframework.example.cache.redis;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.redis.RedisCache;

@Cache("my-cache")
public interface CompositeCache extends RedisCache<CompositeCache.Key, Long> {

    record Key(String userId, String traceId) {}
}
