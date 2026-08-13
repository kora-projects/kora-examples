package io.koraframework.example.s3.kora;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.minio.Bucket;
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio;
import io.goodforgod.testcontainers.extensions.minio.MinioConnection;
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import io.koraframework.s3.client.kora.exception.S3ClientNoSuchKeyException;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

@TestcontainersMinio(
        mode = ContainerMode.PER_RUN,
        bucket = @Bucket(
                value = SyncS3ClientTests.BUCKET,
                create = Bucket.Mode.PER_METHOD,
                drop = Bucket.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class SyncS3ClientTests implements KoraAppTestConfigModifier {

    static final String BUCKET = "simple";

    @ConnectionMinio
    private MinioConnection minioConnection;

    @TestComponent
    private SyncS3Client client;

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
        client.putObject(key, value);

        // when
        try (var found = client.getObject(key); var body = found.body().asInputStream()) {
            assertArrayEquals(value, body.readAllBytes());
        }

        // then
        assertThrows(S3ClientNoSuchKeyException.class, () -> client.getObject("k2"));
    }

    @Test
    void putAndGetObjectAsBytes() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, value);

        // when
        var found = client.getObjectAsBytes(key);

        // then
        assertArrayEquals(value, found);
    }

    @Test
    void putAndGetMeta() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, value);

        // when
        var found = client.getObjectMeta(key);
        assertEquals(value.length, found.size());

        // then
        assertThrows(S3ClientNoSuchKeyException.class, () -> client.getObjectMeta("k2"));
    }

    @Test
    void putAndListObjects() {
        // given
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject("k1", value);
        client.putObject("k2", value);

        // when
        var found = client.listObjects("k");

        // then
        assertEquals(2, found.items().size());
    }

    @Test
    void putAndListObjectKeys() {
        // given
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject("k1", value);
        client.putObject("k2", value);

        // when
        var found = client.listObjectKeys("k");

        // then
        assertIterableEquals(java.util.List.of("pre-k1", "pre-k2"), found);
    }

    @Test
    void putAndDelete() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, value);

        // when
        client.deleteObject(key);

        // then
        assertThrows(S3ClientNoSuchKeyException.class, () -> client.getObject(key));
    }
}
