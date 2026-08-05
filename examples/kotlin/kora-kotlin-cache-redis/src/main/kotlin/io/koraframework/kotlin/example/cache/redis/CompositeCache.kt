package io.koraframework.kotlin.example.cache.redis

import io.koraframework.cache.annotation.Cache
import io.koraframework.cache.redis.RedisCache

@Cache("my-cache")
interface CompositeCache : RedisCache<CompositeCache.Key, Long> {
    data class Key(val userId: String, val traceId: String)
}

