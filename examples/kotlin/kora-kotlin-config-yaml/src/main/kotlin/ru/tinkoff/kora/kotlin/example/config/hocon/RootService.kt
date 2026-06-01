package ru.tinkoff.kora.kotlin.example.config.hocon

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root

@Root
@Component
class RootService(private val fooConfig: FooConfig)
