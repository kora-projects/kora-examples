package ru.tinkoff.kora.example.cache.caffeine;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.caffeine.CaffeineCache;

@Cache("my-cache")
public interface SimpleCache extends CaffeineCache<String, Long> {

}
