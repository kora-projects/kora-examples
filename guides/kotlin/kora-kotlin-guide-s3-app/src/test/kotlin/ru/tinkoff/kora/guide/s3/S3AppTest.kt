package ru.tinkoff.kora.guide.s3

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.minio.Bucket
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio
import io.goodforgod.testcontainers.extensions.minio.MinioConnection
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.s3.controller.DataController
import ru.tinkoff.kora.guide.s3.s3.S3FileClient
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.s3.client.S3NotFoundException
import ru.tinkoff.kora.s3.client.model.S3Body
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import software.amazon.awssdk.services.s3.S3Client
import java.io.ByteArrayInputStream
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

        s3FileClient.uploadFile(
            fileId,
            S3Body.ofInputStream(ByteArrayInputStream(content), content.size.toLong(), "text/plain")
        )

        val downloaded = s3FileClient.downloadFile(fileId)
        assertArrayEquals(content, downloaded.body().asBytes())

        s3FileClient.deleteFile(fileId)
        assertThrows(S3NotFoundException::class.java) { s3FileClient.downloadFile(fileId) }
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
        downloaded.body().write(output)
        assertArrayEquals(content, output.toByteArray())

        val deleteResponse = dataController.deleteFile(uploaded.fileId)
        assertEquals("File deleted successfully", deleteResponse.message)
    }

    companion object {
        const val BUCKET = "guide-s3"
    }
}
