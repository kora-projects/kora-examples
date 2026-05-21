package ru.tinkoff.kora.kotlin.example.s3.aws

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.s3.client.annotation.S3
import ru.tinkoff.kora.s3.client.model.*
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

