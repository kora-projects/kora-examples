package io.koraframework.guide.cache.service;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.caffeine.CaffeineCache;
import io.koraframework.guide.cache.dto.UserResponse;

@Cache("cache.caffeine.users")
public interface UserCaffeineCache extends CaffeineCache<String, UserResponse> {}
