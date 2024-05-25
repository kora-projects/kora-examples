package ru.tinkoff.kora.example.graalvm.crud.cassandra.service;

import ru.tinkoff.kora.cache.annotation.Cache;
import ru.tinkoff.kora.cache.redis.RedisCache;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.model.dao.Pet;
import ru.tinkoff.kora.json.common.annotation.Json;

@Cache("pet-cache")
public interface PetCache extends RedisCache<Long, @Json Pet> {

}
