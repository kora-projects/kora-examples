package ru.tinkoff.kora.kotlin.example.cache.redis

import ru.tinkoff.kora.cache.annotation.Cache
import ru.tinkoff.kora.cache.redis.RedisCache

@Cache("my-cache")
interface SimpleCache : RedisCache<String, Long>

