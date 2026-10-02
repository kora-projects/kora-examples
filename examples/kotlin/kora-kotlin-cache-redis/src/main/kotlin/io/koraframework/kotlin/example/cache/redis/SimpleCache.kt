package io.koraframework.kotlin.example.cache.redis

import io.koraframework.cache.annotation.Cache
import io.koraframework.cache.redis.RedisCache

@Cache("my-cache")
interface SimpleCache : RedisCache<String, Long>

