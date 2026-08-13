package io.koraframework.example.s3.aws;

import io.koraframework.common.annotation.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;

/**
 * Kora 2.0 exposes the AWS SDK {@link S3Client} itself as a component, so working with S3 through
 * this module means working with the AWS SDK API directly. The declarative {@code @S3.Client}
 * contracts live in a different artifact now, see {@code kora-java-s3-client-minio}.
 */
@Component
public class AwsS3Service {

    private final S3Client s3Client;
    private final String bucket;

    public AwsS3Service(S3Client s3Client, S3Config config) {
        this.s3Client = s3Client;
        this.bucket = config.bucket();
    }

    public PutObjectResponse putObject(String key, byte[] value) {
        return s3Client.putObject(r -> r.bucket(bucket).key(key), RequestBody.fromBytes(value));
    }

    public ResponseInputStream<GetObjectResponse> getObject(String key) {
        return s3Client.getObject(r -> r.bucket(bucket).key(key));
    }

    public HeadObjectResponse getObjectMeta(String key) {
        return s3Client.headObject(r -> r.bucket(bucket).key(key));
    }

    public ListObjectsV2Response listObjects(String prefix) {
        return s3Client.listObjectsV2(r -> r.bucket(bucket).prefix(prefix).maxKeys(50));
    }

    public void deleteObject(String key) {
        s3Client.deleteObject(r -> r.bucket(bucket).key(key));
    }

    public DeleteObjectsResponse deleteObjects(List<String> keys) {
        var identifiers = keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        return s3Client.deleteObjects(r -> r.bucket(bucket).delete(d -> d.objects(identifiers)));
    }
}
