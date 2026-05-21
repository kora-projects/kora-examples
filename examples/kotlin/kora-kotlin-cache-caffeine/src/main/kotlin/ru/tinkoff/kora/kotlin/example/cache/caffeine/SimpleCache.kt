package ru.tinkoff.kora.kotlin.example.cache.caffeine

import ru.tinkoff.kora.cache.annotation.Cache
import ru.tinkoff.kora.cache.caffeine.CaffeineCache

@Cache("my-cache")
interface SimpleCache : CaffeineCache<String, Long>

