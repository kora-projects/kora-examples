package io.koraframework.example.cache.caffeine;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.caffeine.CaffeineCache;

@Cache("my-cache")
public interface SimpleCache extends CaffeineCache<String, Long> {

}
