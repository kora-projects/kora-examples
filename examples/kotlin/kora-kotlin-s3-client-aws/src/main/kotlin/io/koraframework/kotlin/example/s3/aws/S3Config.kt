package io.koraframework.kotlin.example.s3.aws

import io.koraframework.config.common.annotation.ConfigSource

@ConfigSource("my")
interface S3Config {
    fun bucket(): String
}
