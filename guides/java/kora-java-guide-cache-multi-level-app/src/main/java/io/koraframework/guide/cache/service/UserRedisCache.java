package io.koraframework.guide.cache.service;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.redis.RedisCache;
import io.koraframework.guide.cache.dto.UserResponse;
import io.koraframework.json.common.annotation.Json;

@Cache("cache.redis.users")
public interface UserRedisCache extends RedisCache<String, @Json UserResponse> {
}
