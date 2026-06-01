package ru.tinkoff.kora.example.s3.aws;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.minio.Bucket;
import io.goodforgod.testcontainers.extensions.minio.ConnectionMinio;
import io.goodforgod.testcontainers.extensions.minio.MinioConnection;
import io.goodforgod.testcontainers.extensions.minio.TestcontainersMinio;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.s3.client.S3NotFoundException;
import ru.tinkoff.kora.s3.client.model.S3Body;
import ru.tinkoff.kora.s3.client.model.S3Object;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import software.amazon.awssdk.services.s3.S3Client;

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
        client.putObject(key, S3Body.ofPublisher(HttpRequest.BodyPublishers.ofByteArray(value), value.length));

        // when
        var found = client.getObject(key);
        assertNotNull(found);
        assertArrayEquals(value, found.body().asBytes());

        // then
        assertThrows(S3NotFoundException.class, () -> client.getObject("k2"));
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
        assertThrows(S3NotFoundException.class, () -> client.getObjectMeta("k2"));
    }

    @Test
    void putAndGetManyObjects() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value));
        client.putObject(key2, S3Body.ofBytes(value));

        // when
        var found = client.getObjects(List.of("pre-" + key1, "pre-" + key2));
        assertEquals(2, found.size());
        for (S3Object object : found) {
            assertEquals("value", new String(object.body().asBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void putAndGetManyMetas() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value));
        client.putObject(key2, S3Body.ofBytes(value));

        // when
        var found = client.getObjectMetas(List.of("pre-" + key1, "pre-" + key2));
        assertEquals(2, found.size());
    }

    @Test
    void putAndListObjects() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value));
        client.putObject(key2, S3Body.ofBytes(value));

        // when
        var found = client.listObject("k");
        assertEquals(2, found.metas().size());
        for (S3Object object : found.objects()) {
            assertEquals("value", new String(object.body().asBytes(), StandardCharsets.UTF_8));
        }
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
        assertEquals(2, found.metas().size());
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
        assertThrows(S3NotFoundException.class, () -> client.getObject("k1"));
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
        assertThrows(S3NotFoundException.class, () -> client.getObject("k1"));
        assertThrows(S3NotFoundException.class, () -> client.getObject("k2"));
    }
}
