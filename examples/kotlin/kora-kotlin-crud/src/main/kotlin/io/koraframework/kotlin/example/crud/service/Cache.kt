package io.koraframework.kotlin.example.crud.service

import io.koraframework.cache.annotation.Cache
import io.koraframework.cache.caffeine.CaffeineCache
import io.koraframework.kotlin.example.crud.model.PetWithCategory

@Cache("pet-cache")
interface PetCache : CaffeineCache<Long, PetWithCategory>
