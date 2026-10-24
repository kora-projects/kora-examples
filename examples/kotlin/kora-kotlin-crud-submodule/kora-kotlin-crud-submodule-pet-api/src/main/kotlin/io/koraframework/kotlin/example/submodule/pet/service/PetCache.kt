package io.koraframework.kotlin.example.submodule.pet.service

import io.koraframework.cache.annotation.Cache
import io.koraframework.cache.caffeine.CaffeineCache
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetWithCategory

@Cache("pet-cache")
interface PetCache : CaffeineCache<Long, PetWithCategory>
