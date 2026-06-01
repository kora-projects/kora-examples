package ru.tinkoff.kora.guide.config.yaml

import ru.tinkoff.kora.config.common.annotation.ConfigValueExtractor
import java.time.Duration

@ConfigValueExtractor
interface LibConfig {
    fun endpoint(): String
    fun requestTimeout(): Duration
}
