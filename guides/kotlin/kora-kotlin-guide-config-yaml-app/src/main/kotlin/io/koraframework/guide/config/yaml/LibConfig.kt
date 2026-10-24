package io.koraframework.guide.config.yaml

import io.koraframework.config.common.annotation.ConfigMapper
import java.time.Duration

@ConfigMapper
interface LibConfig {
    fun endpoint(): String
    fun requestTimeout(): Duration
}
