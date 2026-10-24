package io.koraframework.example.graalvm.crud.cassandra.service;

import io.koraframework.cache.annotation.Cache;
import io.koraframework.cache.redis.RedisCache;
import io.koraframework.example.graalvm.crud.cassandra.model.dao.Pet;
import io.koraframework.json.common.annotation.Json;

@Cache("pet-cache")
public interface PetCache extends RedisCache<Long, @Json Pet> {

}
