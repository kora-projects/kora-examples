package ru.tinkoff.kora.guide.cache.service

import ru.tinkoff.kora.cache.annotation.Cache
import ru.tinkoff.kora.cache.caffeine.CaffeineCache
import ru.tinkoff.kora.guide.cache.dto.UserResponse

@Cache("cache.caffeine.users")
interface UserCaffeineCache : CaffeineCache<String, UserResponse>
