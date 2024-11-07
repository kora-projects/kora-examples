package ru.tinkoff.kora.example.submodule.pet.service;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.caffeine.CaffeineCache;
import ru.tinkoff.kora.example.submodule.pet.model.dao.PetWithCategory;

@Cache("pet-cache")
public interface PetCache extends CaffeineCache<Long, PetWithCategory> {

}
