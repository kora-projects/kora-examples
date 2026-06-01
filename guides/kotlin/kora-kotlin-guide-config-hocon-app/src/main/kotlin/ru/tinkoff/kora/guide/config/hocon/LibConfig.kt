package ru.tinkoff.kora.guide.config.hocon

import ru.tinkoff.kora.config.common.annotation.ConfigValueExtractor
import java.time.Duration

@ConfigValueExtractor
interface LibConfig {
    fun endpoint(): String
    fun requestTimeout(): Duration
}
