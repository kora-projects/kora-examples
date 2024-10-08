package ru.tinkoff.kora.example.s3.aws;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.s3.client.annotation.S3;
import ru.tinkoff.kora.s3.client.model.*;

@S3.Client("my")
public interface AsyncS3Client {

    @S3.Get("pre-{key}")
    CompletableFuture<S3Object> getObject(String key);

    @S3.Get
    CompletableFuture<S3ObjectMeta> getObjectMeta(String key);

    @S3.Get
    CompletableFuture<List<S3Object>> getObjects(List<String> keys);

    @S3.Get
    CompletableFuture<List<S3ObjectMeta>> getObjectMetas(List<String> keys);

    @S3.List("pre-{prefix}")
    CompletionStage<S3ObjectList> listObject(String prefix);

    @S3.List(value = "pre-{prefix}", limit = 50)
    CompletionStage<S3ObjectMetaList> listObjectMeta(String prefix);

    @S3.Put("pre-{key}")
    CompletionStage<S3ObjectUpload> putObject(String key, S3Body object);

    @S3.Delete("pre-{key}")
    CompletionStage<Void> deleteObject(String key);

    @S3.Delete
    CompletionStage<Void> deleteObjects(List<String> keys);
}
