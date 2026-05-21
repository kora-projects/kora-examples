package ru.tinkoff.kora.kotlin.example.cache.caffeine

import jakarta.annotation.Nonnull
import ru.tinkoff.kora.cache.CacheKeyMapper
import ru.tinkoff.kora.cache.annotation.CacheInvalidate
import ru.tinkoff.kora.cache.annotation.CachePut
import ru.tinkoff.kora.cache.annotation.Cacheable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import java.math.BigDecimal
import java.util.concurrent.ThreadLocalRandom

@Root
@Component
open class SimpleService {
    data class UserContext(val userId: String, val traceId: String)

    class UserContextMapping : CacheKeyMapper<String, UserContext> {
        @Nonnull
        override fun map(arg: UserContext): String = arg.userId
    }

    @Mapping(UserContextMapping::class)
    @Cacheable(SimpleCache::class)
    open fun getMapping(context: UserContext): Long = ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @Cacheable(SimpleCache::class)
    open fun get(id: String): Long = ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @CachePut(value = SimpleCache::class, parameters = ["id"])
    open fun put(arg2: BigDecimal, arg3: String, id: String): Long =
        ThreadLocalRandom.current().nextLong(0, 100_000_000L)

    @CacheInvalidate(SimpleCache::class)
    open fun delete(id: String) = Unit

    @CacheInvalidate(value = SimpleCache::class, invalidateAll = true)
    open fun deleteAll() = Unit
}

