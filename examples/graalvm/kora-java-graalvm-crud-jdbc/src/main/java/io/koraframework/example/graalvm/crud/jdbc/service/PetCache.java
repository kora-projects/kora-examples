package io.koraframework.example.graalvm.crud.jdbc.service;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.caffeine.CaffeineCache;
import io.koraframework.example.graalvm.crud.jdbc.model.dao.PetWithCategory;

@Cache("pet-cache")
public interface PetCache extends CaffeineCache<Long, PetWithCategory> {

}
