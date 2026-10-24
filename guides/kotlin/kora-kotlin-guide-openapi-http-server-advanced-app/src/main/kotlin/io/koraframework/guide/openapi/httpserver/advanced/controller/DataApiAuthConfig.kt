package io.koraframework.guide.openapi.httpserver.advanced.controller

import io.koraframework.config.common.annotation.ConfigSource

@ConfigSource("auth.apiKey")
interface DataApiAuthConfig {
    fun value(): String
}
