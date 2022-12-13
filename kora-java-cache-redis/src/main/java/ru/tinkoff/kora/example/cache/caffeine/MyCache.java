package ru.tinkoff.kora.example.cache.caffeine;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.redis.RedisCache;

@Cache("my-cache")
public interface MyCache extends RedisCache<String, Long> {

}
