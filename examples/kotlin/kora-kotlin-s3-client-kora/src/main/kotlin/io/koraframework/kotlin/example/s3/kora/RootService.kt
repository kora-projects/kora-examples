package io.koraframework.kotlin.example.s3.kora

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root

@Root
@Component
class RootService(private val syncS3Client: SyncS3Client)
