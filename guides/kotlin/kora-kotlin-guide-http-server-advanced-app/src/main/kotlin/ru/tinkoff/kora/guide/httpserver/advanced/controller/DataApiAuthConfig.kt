package ru.tinkoff.kora.guide.httpserver.advanced.controller

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("auth.apiKey")
interface DataApiAuthConfig {
    fun value(): String
}
