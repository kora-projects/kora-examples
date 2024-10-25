package ru.tinkoff.kora.example.s3.aws;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.minio.Bucket;
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio;
import io.goodforgod.testcontainers.extensions.minio.MinioConnection;
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.s3.client.model.S3Body;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@TestcontainersMinio(
        mode = ContainerMode.PER_RUN,
        bucket = @Bucket(
                value = AwsS3ClientTests.BUCKET,
                create = Bucket.Mode.PER_METHOD,
                drop = Bucket.Mode.PER_METHOD))
@KoraAppTest(Application.class)
class AwsS3ClientTests implements KoraAppTestConfigModifier {

    static final String BUCKET = "simple";

    @ConnectionMinio
    private MinioConnection minioConnection;

    @TestComponent
    private AwsS3Client client;
    @TestComponent
    private S3Client s3Client;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("S3_URL", minioConnection.params().uri().toString())
                .withSystemProperty("S3_ACCESS_KEY", minioConnection.params().accessKey())
                .withSystemProperty("S3_SECRET_KEY", minioConnection.params().secretKey())
                .withSystemProperty("S3_BUCKET", BUCKET);
    }

    @Test
    void putAndGetObject() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofBytes(value));

        // when
        var found = client.getObject(key);
        assertNotNull(found);

        // then
        assertThrows(NoSuchKeyException.class, () -> client.getObject("k2"));
    }

    @Test
    void putAndGetMeta() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofBytes(value));

        // when
        var found = client.getObjectMeta("pre-" + key);
        assertNotNull(found);

        // then
        assertThrows(NoSuchKeyException.class, () -> client.getObjectMeta("k2"));
    }

    @Test
    void putAndListMetas() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value));
        client.putObject(key2, S3Body.ofBytes(value));

        // when
        var found = client.listObjectMeta("k");
        assertEquals(2, found.contents().size());
    }

    @Test
    void putAndDelete() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofBytes(value));

        // when
        client.deleteObject(key);

        // then
        assertThrows(NoSuchKeyException.class, () -> client.getObject("k1"));
    }

    @Test
    void putAndDeleteMany() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value));
        client.putObject(key2, S3Body.ofBytes(value));

        // when
        client.deleteObjects(List.of("pre-k1", "pre-k2"));
        assertThrows(NoSuchKeyException.class, () -> client.getObject("k1"));
        assertThrows(NoSuchKeyException.class, () -> client.getObject("k2"));
    }
}
