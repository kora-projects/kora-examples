package io.koraframework.kotlin.example.s3.minio

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.s3.client.annotation.S3
import io.koraframework.s3.client.model.*

@Root
@Component
class RootService(private val syncS3Client: SyncS3Client)

