package ru.tinkoff.kora.guide.cache.service;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.caffeine.CaffeineCache;
import ru.tinkoff.kora.guide.cache.dto.UserResponse;

@Cache("cache.caffeine.users")
public interface UserCaffeineCache extends CaffeineCache<String, UserResponse> {}
