package io.koraframework.guide.httpclient.client

import io.koraframework.config.common.annotation.ConfigSource

@ConfigSource("auth.apiKey")
interface ApiKeyAuthConfig {
    fun value(): String
}
