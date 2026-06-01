package ru.tinkoff.kora.kotlin.example.s3.minio

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.minio.Bucket
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio
import io.goodforgod.testcontainers.extensions.minio.MinioConnection
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio
import io.minio.MinioClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.s3.client.S3NotFoundException
import ru.tinkoff.kora.s3.client.model.S3Body
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.nio.charset.StandardCharsets

@TestcontainersMinio(
    mode = ContainerMode.PER_RUN,
    bucket = Bucket(value = [SyncS3ClientTests.BUCKET], create = Bucket.Mode.PER_METHOD, drop = Bucket.Mode.PER_METHOD)
)
@KoraAppTest(Application::class)
class SyncS3ClientTests : KoraAppTestConfigModifier {

    @ConnectionMinio
    lateinit var minioConnection: MinioConnection

    @TestComponent
    lateinit var client: SyncS3Client

    @TestComponent
    lateinit var s3Client: MinioClient

    override fun config(): KoraConfigModification = KoraConfigModification
        .ofSystemProperty("S3_URL", minioConnection.params().uri().toString())
        .withSystemProperty("S3_ACCESS_KEY", minioConnection.params().accessKey())
        .withSystemProperty("S3_SECRET_KEY", minioConnection.params().secretKey())
        .withSystemProperty("S3_BUCKET", BUCKET)

    @Test
    fun putAndGetObject() {
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject(key, S3Body.ofBytes(value))

        val found = client.getObject(key)
        assertNotNull(found)
        assertTrue(value.contentEquals(found.body().asBytes()))

        assertThrows(S3NotFoundException::class.java) { client.getObject("k2") }
    }

    @Test
    fun putAndGetMeta() {
        val key = "k1"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject(key, S3Body.ofBytes(value))

        val found = client.getObjectMeta("pre-$key")
        assertNotNull(found)

        assertThrows(S3NotFoundException::class.java) { client.getObjectMeta("k2") }
    }

    @Test
    fun putAndGetManyObjects() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))
        client.putObject("k2", S3Body.ofBytes(value))

        val found = client.getObjects(listOf("pre-k1", "pre-k2"))
        assertEquals(2, found.size)
        found.forEach { assertEquals("value", String(it.body().asBytes(), StandardCharsets.UTF_8)) }
    }

    @Test
    fun putAndGetManyMetas() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))
        client.putObject("k2", S3Body.ofBytes(value))

        val found = client.getObjectMetas(listOf("pre-k1", "pre-k2"))
        assertEquals(2, found.size)
    }

    @Test
    fun putAndListObjects() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))
        client.putObject("k2", S3Body.ofBytes(value))

        val found = client.listObject("k")
        assertEquals(2, found.metas().size)
        found.objects().forEach { assertEquals("value", String(it.body().asBytes(), StandardCharsets.UTF_8)) }
    }

    @Test
    fun putAndListMetas() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))
        client.putObject("k2", S3Body.ofBytes(value))

        val found = client.listObjectMeta("k")
        assertEquals(2, found.metas().size)
    }

    @Test
    fun putAndDelete() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))

        client.deleteObject("k1")

        assertThrows(S3NotFoundException::class.java) { client.getObject("k1") }
    }

    @Test
    fun putAndDeleteMany() {
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        client.putObject("k1", S3Body.ofBytes(value))
        client.putObject("k2", S3Body.ofBytes(value))

        client.deleteObjects(listOf("pre-k1", "pre-k2"))
        assertThrows(S3NotFoundException::class.java) { client.getObject("k1") }
        assertThrows(S3NotFoundException::class.java) { client.getObject("k2") }
    }

    companion object {
        const val BUCKET = "simple"
    }
}
