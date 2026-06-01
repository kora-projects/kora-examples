package ru.tinkoff.kora.guide.grpcclient.advanced.grpc

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("auth.apiKey")
interface UserStreamingAuthConfig {
    fun value(): String
}
