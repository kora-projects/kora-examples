package io.koraframework.guide.s3.s3

import io.koraframework.s3.client.kora.annotation.S3
import io.koraframework.s3.client.kora.model.response.GetObjectResult
import io.koraframework.s3.client.kora.model.response.ListBucketResult

@S3.Client("s3client.uploads")
@S3.Bucket(".bucket")
interface S3FileClient {

    @S3.Put("files/{fileId}")
    fun uploadFile(fileId: String, body: ByteArray): String

    @S3.Get("files/{fileId}")
    fun downloadFile(fileId: String): GetObjectResult

    @S3.List("files/")
    fun listFiles(): ListBucketResult

    @S3.Delete("files/{fileId}")
    fun deleteFile(fileId: String)
}
