package ru.tinkoff.kora.kotlin.example.submodule.vet.service

import ru.tinkoff.kora.cache.annotation.Cache
import ru.tinkoff.kora.cache.caffeine.CaffeineCache
import ru.tinkoff.kora.kotlin.example.submodule.vet.model.dao.Vet

@Cache("vet-cache")
interface VetCache : CaffeineCache<Long, Vet>
