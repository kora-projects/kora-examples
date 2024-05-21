package ru.tinkoff.kora.example.s3.aws;

import ru.tinkoff.kora.s3.client.annotation.S3;
import ru.tinkoff.kora.s3.client.model.S3Body;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@S3.Client("my")
public interface AwsS3Client {

    @S3.Get("pre-{key}")
    ResponseInputStream<GetObjectResponse> getObject(String key);

    @S3.Get("pre-{key}")
    GetObjectResponse getObjectMeta(String key);

    @S3.List("pre-{prefix}")
    ListObjectsV2Response listObjectMeta(String prefix);

    @S3.Put("pre-{key}")
    PutObjectResponse putObject(String key, S3Body object);

    @S3.Delete("pre-{key}")
    DeleteObjectResponse deleteObject(String key);

    @S3.Delete
    DeleteObjectsResponse deleteObjects(List<String> keys);
}
