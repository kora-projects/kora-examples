package ru.tinkoff.kora.kotlin.example.s3.minio

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.s3.client.annotation.S3
import ru.tinkoff.kora.s3.client.model.S3Body
import ru.tinkoff.kora.s3.client.model.S3Object
import ru.tinkoff.kora.s3.client.model.S3ObjectList
import ru.tinkoff.kora.s3.client.model.S3ObjectMeta
import ru.tinkoff.kora.s3.client.model.S3ObjectMetaList
import ru.tinkoff.kora.s3.client.model.S3ObjectUpload

@S3.Client("my")
interface SyncS3Client {
    @S3.Get("pre-{key}")
    fun getObject(key: String): S3Object

    @S3.Get
    fun getObjectMeta(key: String): S3ObjectMeta

    @S3.Get
    fun getObjects(keys: List<String>): List<S3Object>

    @S3.Get
    fun getObjectMetas(keys: List<String>): List<S3ObjectMeta>

    @S3.List("pre-{prefix}")
    fun listObject(prefix: String): S3ObjectList

    @S3.List(value = "pre-{prefix}", limit = 50)
    fun listObjectMeta(prefix: String): S3ObjectMetaList

    @S3.Put("pre-{key}")
    fun putObject(key: String, obj: S3Body): S3ObjectUpload

    @S3.Delete("pre-{key}")
    fun deleteObject(key: String)

    @S3.Delete
    fun deleteObjects(keys: List<String>)
}

@Root
@Component
class RootService(private val syncS3Client: SyncS3Client)
