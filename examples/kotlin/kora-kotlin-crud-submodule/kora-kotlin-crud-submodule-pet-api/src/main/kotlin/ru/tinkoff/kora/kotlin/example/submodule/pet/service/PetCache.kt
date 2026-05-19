package ru.tinkoff.kora.kotlin.example.submodule.pet.service

import ru.tinkoff.kora.cache.annotation.Cache
import ru.tinkoff.kora.cache.caffeine.CaffeineCache
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.PetWithCategory

@Cache("pet-cache")
interface PetCache : CaffeineCache<Long, PetWithCategory>
