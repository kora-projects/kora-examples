package io.koraframework.kotlin.example.cache.redis

import io.koraframework.cache.CacheKeyMapper
import io.koraframework.cache.annotation.CacheInvalidate
import io.koraframework.cache.annotation.CacheInvalidateAll
import io.koraframework.cache.annotation.CachePut
import io.koraframework.cache.annotation.Cacheable
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Root
import java.math.BigDecimal
import java.util.concurrent.ThreadLocalRandom

@Root
@Component
open class SimpleService {
    @Cacheable(SimpleCache::class)
    open fun get(id: String): Long = ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @CachePut(value = SimpleCache::class, args = ["id"])
    open fun put(arg2: BigDecimal, arg3: String, id: String): Long =
        ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @CacheInvalidate(SimpleCache::class)
    open fun delete(id: String) = Unit

    @CacheInvalidateAll(SimpleCache::class)
    open fun deleteAll() = Unit
}

