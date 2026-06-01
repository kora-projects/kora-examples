package ru.tinkoff.kora.kotlin.example.openapi.http.client

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.kotlin.example.openapi.petV2.api.PetApi

@Root
@Component
class RootService(
    private val petApiV2: PetApi,
    private val petApiV3: ru.tinkoff.kora.kotlin.example.openapi.petV3.api.PetApi,
)
