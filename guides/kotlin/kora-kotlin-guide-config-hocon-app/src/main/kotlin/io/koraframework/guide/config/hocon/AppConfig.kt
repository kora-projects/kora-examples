package io.koraframework.guide.config.hocon

import io.koraframework.config.common.annotation.ConfigSource

@ConfigSource("app")
interface AppConfig {
    fun name(): String
    fun version(): String
    fun environment(): String
}
