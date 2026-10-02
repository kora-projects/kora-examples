package io.koraframework.example.cache.redis;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.redis.RedisCache;

@Cache("my-cache")
public interface SimpleCache extends RedisCache<String, Long> {

}
