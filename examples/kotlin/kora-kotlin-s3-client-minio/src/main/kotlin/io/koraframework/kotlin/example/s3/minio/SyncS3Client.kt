package io.koraframework.kotlin.example.s3.minio

import io.koraframework.s3.client.kora.annotation.S3
import io.koraframework.s3.client.kora.model.response.GetObjectResult
import io.koraframework.s3.client.kora.model.response.HeadObjectResult
import io.koraframework.s3.client.kora.model.response.ListBucketResult

/**
 * Bucket name is read from the `my.bucket` configuration path: a leading dot in `@S3.Bucket`
 * makes the path relative to the client path declared in `@S3.Client`.
 */
@S3.Client("my")
@S3.Bucket(".bucket")
interface SyncS3Client {

    @S3.Get("pre-{key}")
    fun getObject(key: String): GetObjectResult

    @S3.Get("pre-{key}")
    fun getObjectAsBytes(key: String): ByteArray

    @S3.Head("pre-{key}")
    fun getObjectMeta(key: String): HeadObjectResult

    @S3.List("pre-{prefix}")
    fun listObjects(prefix: String): ListBucketResult

    @S3.List("pre-{prefix}")
    fun listObjectKeys(prefix: String): List<String>

    @S3.Put("pre-{key}")
    fun putObject(key: String, value: ByteArray): String

    @S3.Delete("pre-{key}")
    fun deleteObject(key: String)
}
