package io.koraframework.example.s3.minio;

import java.util.List;
import io.koraframework.s3.client.kora.annotation.S3;
import io.koraframework.s3.client.kora.model.response.GetObjectResult;
import io.koraframework.s3.client.kora.model.response.HeadObjectResult;
import io.koraframework.s3.client.kora.model.response.ListBucketResult;

/**
 * Bucket name is read from the {@code my.bucket} configuration path: a leading dot in
 * {@code @S3.Bucket} makes the path relative to the client path declared in {@code @S3.Client}.
 */
@S3.Client("my")
@S3.Bucket(".bucket")
public interface SyncS3Client {

    @S3.Get("pre-{key}")
    GetObjectResult getObject(String key);

    @S3.Get("pre-{key}")
    byte[] getObjectAsBytes(String key);

    @S3.Head("pre-{key}")
    HeadObjectResult getObjectMeta(String key);

    @S3.List("pre-{prefix}")
    ListBucketResult listObjects(String prefix);

    @S3.List("pre-{prefix}")
    List<String> listObjectKeys(String prefix);

    @S3.Put("pre-{key}")
    String putObject(String key, byte[] value);

    @S3.Delete("pre-{key}")
    void deleteObject(String key);
}
