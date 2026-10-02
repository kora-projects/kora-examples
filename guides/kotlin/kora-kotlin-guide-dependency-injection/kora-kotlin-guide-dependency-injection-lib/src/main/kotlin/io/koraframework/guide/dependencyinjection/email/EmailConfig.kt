package io.koraframework.guide.dependencyinjection.email

import io.koraframework.config.common.annotation.ConfigMapper

@ConfigMapper
data class EmailConfig(val topic: String)
