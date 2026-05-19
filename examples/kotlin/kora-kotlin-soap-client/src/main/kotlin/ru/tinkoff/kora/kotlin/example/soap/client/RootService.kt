package ru.tinkoff.kora.kotlin.example.soap.client

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.example.generated.soap.SimpleService

@Root
@Component
class RootService(private val service: SimpleService)
