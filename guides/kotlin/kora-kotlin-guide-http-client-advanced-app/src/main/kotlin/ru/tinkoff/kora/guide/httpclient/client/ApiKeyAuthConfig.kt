package ru.tinkoff.kora.guide.httpclient.client

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("auth.apiKey")
interface ApiKeyAuthConfig {
    fun value(): String
}
