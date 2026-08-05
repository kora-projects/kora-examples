package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.config.common.annotation.ConfigSource

@ConfigSource("auth.apiKey")
interface DataApiAuthConfig {
    fun value(): String
}
