package io.koraframework.kotlin.example.s3.aws

import io.koraframework.common.annotation.Component
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectResponse

/**
 * Kora 2.0 exposes the AWS SDK [S3Client] itself as a component, so working with S3 through this
 * module means working with the AWS SDK API directly. The declarative `@S3.Client` contracts live
 * in a different artifact now, see `kora-kotlin-s3-client-minio`.
 */
@Component
class AwsS3Service(
    private val s3Client: S3Client,
    config: S3Config
) {
    private val bucket: String = config.bucket()

    fun putObject(key: String, value: ByteArray): PutObjectResponse =
        s3Client.putObject({ it.bucket(bucket).key(key) }, RequestBody.fromBytes(value))

    fun getObject(key: String): ResponseInputStream<GetObjectResponse> =
        s3Client.getObject { it.bucket(bucket).key(key) }

    fun getObjectMeta(key: String): HeadObjectResponse =
        s3Client.headObject { it.bucket(bucket).key(key) }

    fun listObjects(prefix: String): ListObjectsV2Response =
        s3Client.listObjectsV2 { it.bucket(bucket).prefix(prefix).maxKeys(50) }

    fun deleteObject(key: String) {
        s3Client.deleteObject { it.bucket(bucket).key(key) }
    }

    fun deleteObjects(keys: List<String>): DeleteObjectsResponse {
        val identifiers = keys.map { ObjectIdentifier.builder().key(it).build() }
        return s3Client.deleteObjects { r -> r.bucket(bucket).delete { d -> d.objects(identifiers) } }
    }
}
