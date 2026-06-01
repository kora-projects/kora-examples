package ru.tinkoff.kora.guide.s3.s3

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class FileMetadata(
    val fileId: String,
    val size: Long?,
    val contentType: String?
)
