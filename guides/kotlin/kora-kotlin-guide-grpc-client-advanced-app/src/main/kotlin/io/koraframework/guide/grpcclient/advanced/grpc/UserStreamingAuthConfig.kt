package io.koraframework.guide.grpcclient.advanced.grpc

import io.koraframework.config.common.annotation.ConfigSource

@ConfigSource("auth.apiKey")
interface UserStreamingAuthConfig {
    fun value(): String
}
