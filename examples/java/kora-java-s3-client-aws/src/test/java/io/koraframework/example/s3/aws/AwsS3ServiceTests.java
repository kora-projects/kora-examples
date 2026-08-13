package io.koraframework.example.s3.aws;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.minio.Bucket;
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio;
import io.goodforgod.testcontainers.extensions.minio.MinioConnection;
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@TestcontainersMinio(
        mode = ContainerMode.PER_RUN,
        bucket = @Bucket(
                value = AwsS3ServiceTests.BUCKET,
                create = Bucket.Mode.PER_METHOD,
                drop = Bucket.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class AwsS3ServiceTests implements KoraAppTestConfigModifier {

    static final String BUCKET = "simple";

    @ConnectionMinio
    private MinioConnection minioConnection;

    @TestComponent
    private AwsS3Service service;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("S3_URL", minioConnection.params().uri().toString())
                .withSystemProperty("S3_ACCESS_KEY", minioConnection.params().accessKey())
                .withSystemProperty("S3_SECRET_KEY", minioConnection.params().secretKey())
                .withSystemProperty("S3_BUCKET", BUCKET);
    }

    @Test
    void putAndGetObject() throws IOException {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        service.putObject(key, value);

        // when
        try (var found = service.getObject(key)) {
            assertArrayEquals(value, found.readAllBytes());
        }

        // then
        assertThrows(NoSuchKeyException.class, () -> service.getObject("k2"));
    }

    @Test
    void putAndGetMeta() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        service.putObject(key, value);

        // when
        var found = service.getObjectMeta(key);
        assertEquals(value.length, found.contentLength());

        // then
        assertThrows(NoSuchKeyException.class, () -> service.getObjectMeta("k2"));
    }

    @Test
    void putAndListObjects() {
        // given
        var value = "value".getBytes(StandardCharsets.UTF_8);
        service.putObject("k1", value);
        service.putObject("k2", value);

        // when
        var found = service.listObjects("k");

        // then
        assertEquals(2, found.contents().size());
    }

    @Test
    void putAndDelete() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        service.putObject(key, value);

        // when
        service.deleteObject(key);

        // then
        assertThrows(NoSuchKeyException.class, () -> service.getObject(key));
    }

    @Test
    void putAndDeleteMany() {
        // given
        var value = "value".getBytes(StandardCharsets.UTF_8);
        service.putObject("k1", value);
        service.putObject("k2", value);

        // when
        service.deleteObjects(List.of("k1", "k2"));

        // then
        assertThrows(NoSuchKeyException.class, () -> service.getObject("k1"));
        assertThrows(NoSuchKeyException.class, () -> service.getObject("k2"));
    }
}
