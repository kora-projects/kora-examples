package ru.tinkoff.kora.example.graalvm.crud.r2dbc.service;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.caffeine.CaffeineCache;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.PetWithCategory;

@Cache("pet-cache")
public interface PetCache extends CaffeineCache<Long, PetWithCategory> {

}
