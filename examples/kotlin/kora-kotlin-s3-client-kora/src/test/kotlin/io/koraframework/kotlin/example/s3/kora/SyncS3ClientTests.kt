package io.koraframework.kotlin.example.s3.kora

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.minio.Bucket
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio
import io.goodforgod.testcontainers.extensions.minio.MinioConnection
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import io.koraframework.s3.client.kora.exception.S3ClientNoSuchKeyException
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent
import java.nio.charset.StandardCharsets

@TestcontainersMinio(
    mode = ContainerMode.PER_RUN,
    bucket = Bucket(
        value = [SyncS3ClientTests.BUCKET],
        create = Bucket.Mode.PER_METHOD,
        drop = Bucket.Mode.PER_METHOD
    )
)
@KoraAppTest(Application::class)
class SyncS3ClientTests : KoraAppTestConfigModifier {

    companion object {
        const val BUCKET = "simple"
    }

    @ConnectionMinio
    lateinit var minioConnection: MinioConnection

    @TestComponent
    lateinit var client: SyncS3Client

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
        client.putObject(key, value)

        // when
        client.getObject(key).use { found ->
            found.body().asInputStream().use { body ->
                assertArrayEquals(value, body.readAllBytes())
            }
        }

        // then
        assertThrows(S3ClientNoSuchKeyException::class.java) { client.getObject("k2") }
    }

    @Test
    fun putAndGetObjectAsBytes() {
        // given
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject(key, value)

        // when
        val found = client.getObjectAsBytes(key)

        // then
        assertArrayEquals(value, found)
    }

    @Test
    fun putAndGetMeta() {
        // given
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject(key, value)

        // when
        val found = client.getObjectMeta(key)
        assertEquals(value.size.toLong(), found.size())

        // then
        assertThrows(S3ClientNoSuchKeyException::class.java) { client.getObjectMeta("k2") }
    }

    @Test
    fun putAndListObjects() {
        // given
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", value)
        client.putObject("k2", value)

        // when
        val found = client.listObjects("k")

        // then
        assertEquals(2, found.items().size)
    }

    @Test
    fun putAndListObjectKeys() {
        // given
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", value)
        client.putObject("k2", value)

        // when
        val found = client.listObjectKeys("k")

        // then
        assertIterableEquals(listOf("pre-k1", "pre-k2"), found)
    }

    @Test
    fun putAndDelete() {
        // given
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject(key, value)

        // when
        client.deleteObject(key)

        // then
        assertThrows(S3ClientNoSuchKeyException::class.java) { client.getObject(key) }
    }
}
