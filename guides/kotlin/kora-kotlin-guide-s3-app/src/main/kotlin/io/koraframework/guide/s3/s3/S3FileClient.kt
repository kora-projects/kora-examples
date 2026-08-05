package io.koraframework.guide.s3.s3

import io.koraframework.s3.client.annotation.S3
import io.koraframework.s3.client.model.S3Body
import io.koraframework.s3.client.model.S3Object
import io.koraframework.s3.client.model.S3ObjectList
import io.koraframework.s3.client.model.S3ObjectUpload

@S3.Client("s3client.uploads")
interface S3FileClient {

    @S3.Put("files/{fileId}")
    fun uploadFile(fileId: String, body: S3Body): S3ObjectUpload

    @S3.Get("files/{fileId}")
    fun downloadFile(fileId: String): S3Object

    @S3.List("files/")
    fun listFiles(): S3ObjectList

    @S3.Delete("files/{fileId}")
    fun deleteFile(fileId: String)
}
