package io.koraframework.kotlin.example.s3.aws

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.s3.client.annotation.S3
import io.koraframework.s3.client.model.*
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.PutObjectResponse

@Root
@Component
class RootService(
    private val syncS3Client: SyncS3Client,
    private val awsS3Client: AwsS3Client
)

