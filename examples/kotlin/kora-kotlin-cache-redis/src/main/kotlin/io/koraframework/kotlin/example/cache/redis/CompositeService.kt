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
open class CompositeService {
    data class UserContext(val userId: String, val traceId: String)

    @Component
    class UserContextMapping : CacheKeyMapper<CompositeCache.Key, UserContext> {
                override fun map(arg: UserContext): CompositeCache.Key = CompositeCache.Key(arg.userId, arg.traceId)
    }

    @Mapping(UserContextMapping::class)
    @Cacheable(CompositeCache::class)
    open fun getMapping(context: UserContext): Long = ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @Cacheable(CompositeCache::class)
    open fun get(id: String, traceId: String): Long = ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @CachePut(value = CompositeCache::class, args = ["id", "traceId"])
    open fun put(arg2: BigDecimal, arg3: String, id: String, traceId: String): Long =
        ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @CacheInvalidate(CompositeCache::class)
    open fun delete(id: String, traceId: String) = Unit

    @CacheInvalidateAll(CompositeCache::class)
    open fun deleteAll() = Unit
}

