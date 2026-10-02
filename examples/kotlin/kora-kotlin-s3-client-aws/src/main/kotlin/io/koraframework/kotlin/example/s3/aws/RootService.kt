package io.koraframework.kotlin.example.s3.aws

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root

@Root
@Component
class RootService(
    private val awsS3Service: AwsS3Service
)
