package ru.tinkoff.kora.example.submodule.vet.service;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.caffeine.CaffeineCache;
import ru.tinkoff.kora.example.submodule.vet.model.dao.Vet;

@Cache("vet-cache")
public interface VetCache extends CaffeineCache<Long, Vet> {

}
