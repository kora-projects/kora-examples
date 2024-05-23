package ru.tinkoff.kora.example.s3.aws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
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
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@KoraAppTest(Application.class)
class AsyncS3ClientTests implements KoraAppTestConfigModifier {

    @Container
    private static final MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2024-05-10T01-41-38Z");

    @TestComponent
    private AsyncS3Client client;
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
            client.deleteObjects(List.of("pre-k1", "pre-k2")).toCompletableFuture().join();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void putAndGetObject() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofPublisher(HttpRequest.BodyPublishers.ofByteArray(value), value.length)).toCompletableFuture().join();

        // when
        var found = client.getObject(key).join();
        assertNotNull(found);
        assertTrue(Arrays.equals(value, found.body().asBytes()));

        // then
        var ex = assertThrows(CompletionException.class, () -> client.getObject("k2").join());
        assertInstanceOf(S3NotFoundException.class, ex.getCause());
    }

    @Test
    void putAndGetMeta() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofBytes(value)).toCompletableFuture().join();

        // when
        var found = client.getObjectMeta(key).join();
        assertNotNull(found);

        // then
        var ex = assertThrows(CompletionException.class, () -> client.getObject("k2").join());
        assertInstanceOf(S3NotFoundException.class, ex.getCause());
    }

    @Test
    void putAndGetManyObjects() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value)).toCompletableFuture().join();
        client.putObject(key2, S3Body.ofBytes(value)).toCompletableFuture().join();

        // when
        var found = client.getObjects(List.of("pre-" + key1, "pre-" + key2)).join();
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
        client.putObject(key1, S3Body.ofBytes(value)).toCompletableFuture().join();
        client.putObject(key2, S3Body.ofBytes(value)).toCompletableFuture().join();

        // when
        var found = client.getObjectMetas(List.of("pre-" + key1, "pre-" + key2)).join();
        assertEquals(2, found.size());
    }

    @Test
    void putAndListObjects() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value)).toCompletableFuture().join();
        client.putObject(key2, S3Body.ofBytes(value)).toCompletableFuture().join();

        // when
        var found = client.listObject("k").toCompletableFuture().join();
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
        client.putObject(key1, S3Body.ofBytes(value)).toCompletableFuture().join();
        client.putObject(key2, S3Body.ofBytes(value)).toCompletableFuture().join();

        // when
        var found = client.listObjectMeta("k").toCompletableFuture().join();
        assertEquals(2, found.metas().size());
    }

    @Test
    void putAndDelete() {
        // given
        var key = "k1";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key, S3Body.ofBytes(value)).toCompletableFuture().join();

        // when
        client.deleteObject(key).toCompletableFuture().join();

        // then
        var ex = assertThrows(CompletionException.class, () -> client.getObject("k1").join());
        assertInstanceOf(S3NotFoundException.class, ex.getCause());
    }

    @Test
    void putAndDeleteMany() {
        // given
        var key1 = "k1";
        var key2 = "k2";
        var value = "value".getBytes(StandardCharsets.UTF_8);
        client.putObject(key1, S3Body.ofBytes(value)).toCompletableFuture().join();
        client.putObject(key2, S3Body.ofBytes(value)).toCompletableFuture().join();

        // when
        client.deleteObjects(List.of("pre-k1", "pre-k2")).toCompletableFuture().join();
        var ex1 = assertThrows(CompletionException.class, () -> client.getObject("k1").join());
        assertInstanceOf(S3NotFoundException.class, ex1.getCause());
        var ex2 = assertThrows(CompletionException.class, () -> client.getObject("k2").join());
        assertInstanceOf(S3NotFoundException.class, ex2.getCause());
    }
}
