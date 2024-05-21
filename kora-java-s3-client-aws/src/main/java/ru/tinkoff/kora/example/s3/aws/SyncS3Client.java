package ru.tinkoff.kora.example.s3.aws;

import ru.tinkoff.kora.s3.client.annotation.S3;
import ru.tinkoff.kora.s3.client.model.*;

import java.util.List;

@S3.Client("my")
public interface SyncS3Client {

    @S3.Get("pre-{key}")
    S3Object getObject(String key);

    @S3.Get("pre-{key}")
    S3ObjectMeta getObjectMeta(String key);

    @S3.Get
    List<S3Object> getObjects(List<String> keys);

    @S3.Get
    List<S3ObjectMeta> getObjectMetas(List<String> keys);

    @S3.List("pre-{prefix}")
    S3ObjectList listObject(String prefix);

    @S3.List("pre-{prefix}")
    S3ObjectMetaList listObjectMeta(String prefix);

    @S3.Put("pre-{key}")
    String putObject(String key, S3Body object);

    @S3.Delete("pre-{key}")
    void deleteObject(String key);

    @S3.Delete
    void deleteObjects(List<String> keys);
}
