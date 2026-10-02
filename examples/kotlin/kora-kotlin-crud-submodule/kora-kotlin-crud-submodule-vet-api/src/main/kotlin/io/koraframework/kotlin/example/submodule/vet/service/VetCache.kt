package io.koraframework.kotlin.example.submodule.vet.service

import io.koraframework.cache.annotation.Cache
import io.koraframework.cache.caffeine.CaffeineCache
import io.koraframework.kotlin.example.submodule.vet.model.dao.Vet

@Cache("vet-cache")
interface VetCache : CaffeineCache<Long, Vet>
