package ru.tinkoff.kora.kotlin.example.s3.minio

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.s3.client.annotation.S3
import ru.tinkoff.kora.s3.client.model.*

@Root
@Component
class RootService(private val syncS3Client: SyncS3Client)

