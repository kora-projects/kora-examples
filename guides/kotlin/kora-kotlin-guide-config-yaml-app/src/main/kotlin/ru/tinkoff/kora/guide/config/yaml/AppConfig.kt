package ru.tinkoff.kora.guide.config.yaml

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("app")
interface AppConfig {
    fun name(): String
    fun version(): String
    fun environment(): String
}
