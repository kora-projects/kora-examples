package ru.tinkoff.kora.example.s3.aws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.s3.client.S3NotFoundException;
import ru.tinkoff.kora.s3.client.model.S3Body;
import ru.tinkoff.kora.s3.client.model.S3Object;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@KoraAppTest(Application.class)
class ReactorS3ClientTests implements KoraAppTestConfigModifier {

    @Container
    private static final MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2024-08-03T04-33-23Z")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(MinIOContainer.class)));

    @TestComponent
    private ReactorS3Client client;
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
            client.deleteObjects(List.of("pre-k1", "pre-k2")).block();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void putAndGetObject() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofPublisher(HttpRequest.BodyPublishers.ofByteArray(value), value.length)).block();

        // when
        var found = client.getObject(key).block();
        assertNotNull(found);
        assertArrayEquals(value, found.body().asBytes());

        // then
        Optional<S3Object> k2 = client.getObject("k2").blockOptional();
        assertTrue(k2.isEmpty());
    }

    @Test
    void putAndGetMeta() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofBytes(value)).block();

        // when
        var found = client.getObjectMeta("pre-" + key).block();
        assertNotNull(found);

        // then
        Optional<S3Object> k2 = client.getObject("k2").blockOptional();
        assertTrue(k2.isEmpty());
    }

    @Test
    void putAndGetManyObjects() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value)).block();
        client.putObject(key2, S3Body.ofBytes(value)).block();

        // when
        var found = client.getObjects(List.of("pre-" + key1, "pre-" + key2)).block();
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
        client.putObject(key1, S3Body.ofBytes(value)).block();
        client.putObject(key2, S3Body.ofBytes(value)).block();

        // when
        var found = client.getObjectMetas(List.of("pre-" + key1, "pre-" + key2)).block();
        assertEquals(2, found.size());
    }

    @Test
    void putAndListObjects() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value)).block();
        client.putObject(key2, S3Body.ofBytes(value)).block();

        // when
        var found = client.listObject("k").block();
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
        client.putObject(key1, S3Body.ofBytes(value)).block();
        client.putObject(key2, S3Body.ofBytes(value)).block();

        // when
        var found = client.listObjectMeta("k").block();
        assertEquals(2, found.metas().size());
    }

    @Test
    void putAndDelete() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofBytes(value)).block();

        // when
        client.deleteObject(key).block();

        // then
        Optional<S3Object> k1 = client.getObject("k1").blockOptional();
        assertTrue(k1.isEmpty());
    }

    @Test
    void putAndDeleteMany() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value)).block();
        client.putObject(key2, S3Body.ofBytes(value)).block();

        // when
        client.deleteObjects(List.of("pre-k1", "pre-k2")).block();
        Optional<S3Object> k1 = client.getObject("k1").blockOptional();
        assertTrue(k1.isEmpty());
        Optional<S3Object> k2 = client.getObject("k2").blockOptional();
        assertTrue(k2.isEmpty());
    }
}
