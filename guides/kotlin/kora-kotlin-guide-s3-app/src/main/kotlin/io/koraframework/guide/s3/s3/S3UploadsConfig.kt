package io.koraframework.guide.s3.s3

import io.koraframework.config.common.annotation.ConfigSource

/**
 * The declarative client reads its bucket through `@S3.Bucket`, which produces a generated class
 * rather than an injectable config, so bucket administration reads the same path itself.
 */
@ConfigSource("s3client.uploads")
interface S3UploadsConfig {
    fun bucket(): String
}
