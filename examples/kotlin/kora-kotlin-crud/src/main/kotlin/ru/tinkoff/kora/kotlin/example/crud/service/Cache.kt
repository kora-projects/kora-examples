package ru.tinkoff.kora.kotlin.example.crud.service

import ru.tinkoff.kora.cache.annotation.Cache
import ru.tinkoff.kora.cache.caffeine.CaffeineCache
import ru.tinkoff.kora.kotlin.example.crud.model.PetWithCategory

@Cache("pet-cache")
interface PetCache : CaffeineCache<Long, PetWithCategory>
