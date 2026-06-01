package ru.tinkoff.kora.guide.s3;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.minio.Bucket;
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio;
import io.goodforgod.testcontainers.extensions.minio.MinioConnection;
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.s3.controller.DataController;
import ru.tinkoff.kora.guide.s3.s3.S3FileClient;
import ru.tinkoff.kora.guide.s3.s3.FileMetadata;
import ru.tinkoff.kora.http.common.form.FormMultipart;
import ru.tinkoff.kora.s3.client.S3NotFoundException;
import ru.tinkoff.kora.s3.client.model.S3Body;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

@TestcontainersMinio(
        mode = ContainerMode.PER_RUN,
        bucket = @Bucket(value = S3AppTest.BUCKET, create = Bucket.Mode.PER_METHOD, drop = Bucket.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class S3AppTest implements KoraAppTestConfigModifier {

    static final String BUCKET = "guide-s3";

    @ConnectionMinio
    private MinioConnection minioConnection;

    @TestComponent
    private S3FileClient s3FileClient;
    @TestComponent
    private DataController dataController;
    @TestComponent
    private S3Client s3Client;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("S3_URL", minioConnection.params().uri().toString())
                .withSystemProperty("S3_ACCESS_KEY", minioConnection.params().accessKey())
                .withSystemProperty("S3_SECRET_KEY", minioConnection.params().secretKey())
                .withSystemProperty("S3_BUCKET", BUCKET);
    }

    @Test
    void declarativeClientCrudWorks() {
        var content = "guide-client-body".getBytes(StandardCharsets.UTF_8);
        var fileId = "file-1";

        this.s3FileClient.uploadFile(fileId, S3Body.ofInputStream(new ByteArrayInputStream(content), content.length, "text/plain"));

        var downloaded = this.s3FileClient.downloadFile(fileId);
        assertArrayEquals(content, downloaded.body().asBytes());

        this.s3FileClient.deleteFile(fileId);
        assertThrows(S3NotFoundException.class, () -> this.s3FileClient.downloadFile(fileId));
    }

    @Test
    void dataControllerExtendsHttpServerUploadFlow() throws Exception {
        var content = "controller upload".getBytes(StandardCharsets.UTF_8);
        var multipart = new FormMultipart(List.of(FormMultipart.file("file", "controller.txt", "text/plain", content)));

        FileMetadata uploaded = this.dataController.uploadFile(multipart);
        assertNotNull(uploaded.fileId());
        assertEquals(content.length, uploaded.size());

        var downloaded = this.dataController.downloadFile(uploaded.fileId());
        assertEquals(200, downloaded.code());
        var output = new ByteArrayOutputStream();
        downloaded.body().write(output);
        assertArrayEquals(content, output.toByteArray());

        var deleteResponse = this.dataController.deleteFile(uploaded.fileId());
        assertEquals("File deleted successfully", deleteResponse.message());
    }
}
