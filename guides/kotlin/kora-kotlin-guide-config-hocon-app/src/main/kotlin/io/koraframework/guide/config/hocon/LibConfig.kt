package io.koraframework.guide.config.hocon

import io.koraframework.config.common.annotation.ConfigMapper
import java.time.Duration

@ConfigMapper
interface LibConfig {
    fun endpoint(): String
    fun requestTimeout(): Duration
}
