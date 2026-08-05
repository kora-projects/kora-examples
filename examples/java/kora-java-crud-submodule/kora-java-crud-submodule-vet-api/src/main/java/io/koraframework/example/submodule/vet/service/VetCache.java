package io.koraframework.example.submodule.vet.service;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.caffeine.CaffeineCache;
import io.koraframework.example.submodule.vet.model.dao.Vet;

@Cache("vet-cache")
public interface VetCache extends CaffeineCache<Long, Vet> {

}
