package ru.tinkoff.kora.guide.cache.service;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.redis.RedisCache;
import ru.tinkoff.kora.guide.cache.dto.UserResponse;
import ru.tinkoff.kora.json.common.annotation.Json;

@Cache("cache.redis.users")
public interface UserRedisCache extends RedisCache<String, @Json UserResponse> {
}
