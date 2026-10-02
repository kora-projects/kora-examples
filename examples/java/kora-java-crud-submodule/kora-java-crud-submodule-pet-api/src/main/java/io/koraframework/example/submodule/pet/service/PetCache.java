package io.koraframework.example.submodule.pet.service;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.caffeine.CaffeineCache;
import io.koraframework.example.submodule.pet.model.dao.PetWithCategory;

@Cache("pet-cache")
public interface PetCache extends CaffeineCache<Long, PetWithCategory> {

}
