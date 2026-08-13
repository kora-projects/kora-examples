package io.koraframework.kotlin.example.s3.aws

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.minio.Bucket
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio
import io.goodforgod.testcontainers.extensions.minio.MinioConnection
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.nio.charset.StandardCharsets

@TestcontainersMinio(
    mode = ContainerMode.PER_RUN,
    bucket = Bucket(
        value = [AwsS3ServiceTests.BUCKET],
        create = Bucket.Mode.PER_METHOD,
        drop = Bucket.Mode.PER_METHOD
    )
)
@KoraAppTest(Application::class)
class AwsS3ServiceTests : KoraAppTestConfigModifier {

    companion object {
        const val BUCKET = "simple"
    }

    @ConnectionMinio
    lateinit var minioConnection: MinioConnection

    @TestComponent
    lateinit var service: AwsS3Service

    override fun config(): KoraConfigModification = KoraConfigModification
        .ofSystemProperty("S3_URL", minioConnection.params().uri().toString())
        .withSystemProperty("S3_ACCESS_KEY", minioConnection.params().accessKey())
        .withSystemProperty("S3_SECRET_KEY", minioConnection.params().secretKey())
        .withSystemProperty("S3_BUCKET", BUCKET)

    @Test
    fun putAndGetObject() {
        // given
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        service.putObject(key, value)

        // when
        service.getObject(key).use { found ->
            assertArrayEquals(value, found.readAllBytes())
        }

        // then
        assertThrows(NoSuchKeyException::class.java) { service.getObject("k2") }
    }

    @Test
    fun putAndGetMeta() {
        // given
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        service.putObject(key, value)

        // when
        val found = service.getObjectMeta(key)
        assertEquals(value.size.toLong(), found.contentLength())

        // then
        assertThrows(NoSuchKeyException::class.java) { service.getObjectMeta("k2") }
    }

    @Test
    fun putAndListObjects() {
        // given
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        service.putObject("k1", value)
        service.putObject("k2", value)

        // when
        val found = service.listObjects("k")

        // then
        assertEquals(2, found.contents().size)
    }

    @Test
    fun putAndDelete() {
        // given
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        service.putObject(key, value)

        // when
        service.deleteObject(key)

        // then
        assertThrows(NoSuchKeyException::class.java) { service.getObject(key) }
    }

    @Test
    fun putAndDeleteMany() {
        // given
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        service.putObject("k1", value)
        service.putObject("k2", value)

        // when
        service.deleteObjects(listOf("k1", "k2"))

        // then
        assertThrows(NoSuchKeyException::class.java) { service.getObject("k1") }
        assertThrows(NoSuchKeyException::class.java) { service.getObject("k2") }
    }
}
