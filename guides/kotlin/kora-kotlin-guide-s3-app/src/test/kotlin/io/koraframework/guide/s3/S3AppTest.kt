package io.koraframework.guide.s3

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.minio.Bucket
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio
import io.goodforgod.testcontainers.extensions.minio.MinioConnection
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import io.koraframework.guide.s3.controller.DataController
import io.koraframework.guide.s3.s3.S3FileClient
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.s3.client.kora.exception.S3ClientNoSuchKeyException
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent
import software.amazon.awssdk.services.s3.S3Client
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

@TestcontainersMinio(
    mode = ContainerMode.PER_RUN,
    bucket = Bucket(value = [S3AppTest.BUCKET], create = Bucket.Mode.PER_METHOD, drop = Bucket.Mode.PER_METHOD)
)
@KoraAppTest(Application::class)
class S3AppTest : KoraAppTestConfigModifier {
    @ConnectionMinio
    lateinit var minioConnection: MinioConnection

    @TestComponent
    lateinit var s3FileClient: S3FileClient

    @TestComponent
    lateinit var dataController: DataController

    @TestComponent
    lateinit var s3Client: S3Client

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("S3_URL", minioConnection.params().uri().toString())
            .withSystemProperty("S3_ACCESS_KEY", minioConnection.params().accessKey())
            .withSystemProperty("S3_SECRET_KEY", minioConnection.params().secretKey())
            .withSystemProperty("S3_BUCKET", BUCKET)

    @Test
    fun declarativeClientCrudWorks() {
        val content = "guide-client-body".toByteArray(StandardCharsets.UTF_8)
        val fileId = "file-1"

        s3FileClient.uploadFile(fileId, content)

        s3FileClient.downloadFile(fileId).use { downloaded ->
            downloaded.body().asInputStream().use { body ->
                assertArrayEquals(content, body.readAllBytes())
            }
        }

        s3FileClient.deleteFile(fileId)
        assertThrows(S3ClientNoSuchKeyException::class.java) { s3FileClient.downloadFile(fileId) }
    }

    @Test
    fun dataControllerExtendsHttpServerUploadFlow() {
        val content = "controller upload".toByteArray(StandardCharsets.UTF_8)
        val multipart = FormMultipart(listOf(FormMultipart.file("file", "controller.txt", "text/plain", content)))

        val uploaded = dataController.uploadFile(multipart)
        assertNotNull(uploaded.fileId)
        assertEquals(content.size.toLong(), uploaded.size)

        val downloaded = dataController.downloadFile(uploaded.fileId)
        assertEquals(200, downloaded.code())
        val output = ByteArrayOutputStream()
        downloaded.body()!!.write(output)
        assertArrayEquals(content, output.toByteArray())

        val deleteResponse = dataController.deleteFile(uploaded.fileId)
        assertEquals("File deleted successfully", deleteResponse.message)
    }

    companion object {
        const val BUCKET = "guide-s3"
    }
}
