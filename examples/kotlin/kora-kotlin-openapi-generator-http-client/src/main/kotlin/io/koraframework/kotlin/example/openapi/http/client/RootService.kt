package io.koraframework.kotlin.example.openapi.http.client

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.kotlin.example.openapi.petV2.api.PetApi

@Root
@Component
class RootService(
    private val petApiV2: PetApi,
    private val petApiV3: io.koraframework.kotlin.example.openapi.petV3.api.PetApi,
)
