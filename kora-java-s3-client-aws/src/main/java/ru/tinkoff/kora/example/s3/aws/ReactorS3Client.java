package ru.tinkoff.kora.example.s3.aws;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.s3.client.annotation.S3;
import ru.tinkoff.kora.s3.client.model.*;

import java.util.List;

@S3.Client("my")
public interface ReactorS3Client {

    @S3.Get("pre-{key}")
    Mono<S3Object> getObject(String key);

    @S3.Get("pre-{key}")
    Mono<S3ObjectMeta> getObjectMeta(String key);

    @S3.Get
    Mono<List<S3Object>> getObjects(List<String> keys);

    @S3.Get
    Mono<List<S3ObjectMeta>> getObjectMetas(List<String> keys);

    @S3.List("pre-{prefix}")
    Mono<S3ObjectList> listObject(String prefix);

    @S3.List(value = "pre-{prefix}", limit = 50)
    Mono<S3ObjectMetaList> listObjectMeta(String prefix);

    @S3.Put("pre-{key}")
    Mono<S3ObjectUpload> putObject(String key, S3Body object);

    @S3.Delete("pre-{key}")
    Mono<Void> deleteObject(String key);

    @S3.Delete
    Mono<Void> deleteObjects(List<String> keys);
}
