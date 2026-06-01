package ru.tinkoff.kora.example.crud.service;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.caffeine.CaffeineCache;
import ru.tinkoff.kora.example.crud.model.dao.PetWithCategory;

@Cache("pet-cache")
public interface PetCache extends CaffeineCache<Long, PetWithCategory> {

}
