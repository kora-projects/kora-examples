package io.koraframework.kotlin.example.cache.caffeine

import io.koraframework.cache.annotation.Cache
import io.koraframework.cache.caffeine.CaffeineCache

@Cache("my-cache")
interface SimpleCache : CaffeineCache<String, Long>

