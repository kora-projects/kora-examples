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

@S3.Client("my")
interface AwsS3Client {
    @S3.Get("pre-{key}")
    fun getObject(key: String): ResponseInputStream<GetObjectResponse>

    @S3.Get
    fun getObjectMeta(key: String): GetObjectResponse

    @S3.List(value = "pre-{prefix}", limit = 50)
    fun listObjectMeta(prefix: String): ListObjectsV2Response

    @S3.Put("pre-{key}")
    fun putObject(key: String, obj: S3Body): PutObjectResponse

    @S3.Delete("pre-{key}")
    fun deleteObject(key: String): DeleteObjectResponse
}

