package ru.tinkoff.kora.kotlin.example.s3.aws

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.minio.Bucket
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio
import io.goodforgod.testcontainers.extensions.minio.MinioConnection
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.s3.client.model.S3Body
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.nio.charset.StandardCharsets

@TestcontainersMinio(
    mode = ContainerMode.PER_RUN,
    bucket = Bucket(value = [AwsS3ClientTests.BUCKET], create = Bucket.Mode.PER_METHOD, drop = Bucket.Mode.PER_METHOD)
)
@KoraAppTest(Application::class)
class AwsS3ClientTests : KoraAppTestConfigModifier {

    @ConnectionMinio
    lateinit var minioConnection: MinioConnection

    @TestComponent
    lateinit var client: AwsS3Client

    @TestComponent
    lateinit var s3Client: S3Client

    override fun config(): KoraConfigModification = KoraConfigModification
        .ofSystemProperty("S3_URL", minioConnection.params().uri().toString())
        .withSystemProperty("S3_ACCESS_KEY", minioConnection.params().accessKey())
        .withSystemProperty("S3_SECRET_KEY", minioConnection.params().secretKey())
        .withSystemProperty("S3_BUCKET", BUCKET)

    @Test
    fun putAndGetObject() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))

        val found = client.getObject("k1")
        assertNotNull(found)

        assertThrows(NoSuchKeyException::class.java) { client.getObject("k2") }
    }

    @Test
    fun putAndGetMeta() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))

        val found = client.getObjectMeta("pre-k1")
        assertNotNull(found)

        assertThrows(NoSuchKeyException::class.java) { client.getObjectMeta("k2") }
    }

    @Test
    fun putAndListMetas() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))
        client.putObject("k2", S3Body.ofBytes(value))

        val found = client.listObjectMeta("k")
        assertEquals(2, found.contents().size)
    }

    @Test
    fun putAndDelete() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))

        client.deleteObject("k1")

        assertThrows(NoSuchKeyException::class.java) { client.getObject("k1") }
    }

    companion object {
        const val BUCKET = "simple"
    }
}
