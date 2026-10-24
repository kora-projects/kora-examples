package io.koraframework.guide.s3.s3

import io.koraframework.json.common.annotation.Json

@Json
data class FileMetadata(
    val fileId: String,
    val size: Long?,
    val contentType: String?
)
