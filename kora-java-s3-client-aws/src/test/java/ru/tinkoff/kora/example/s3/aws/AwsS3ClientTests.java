package ru.tinkoff.kora.example.s3.aws;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.s3.client.model.S3Body;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Testcontainers
@KoraAppTest(Application.class)
class AwsS3ClientTests implements KoraAppTestConfigModifier {

    @Container
    private static final MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2024-08-03T04-33-23Z")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(MinIOContainer.class)));

    @TestComponent
    private AwsS3Client client;
    @TestComponent
    private S3Client s3Client;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("S3_URL", container.getS3URL())
                .withSystemProperty("S3_ACCESS_KEY", container.getUserName())
                .withSystemProperty("S3_SECRET_KEY", container.getPassword())
                .withSystemProperty("S3_BUCKET", "simple");
    }

    @BeforeEach
    void cleanup() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket("simple")
                    .build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            // ignore
        } catch (Exception e) {
            throw e;
        }

        try {
            client.deleteObjects(List.of("pre-k1", "pre-k2"));
        } catch (Exception e) {
            // ignore
        }
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
