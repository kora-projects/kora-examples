package io.koraframework.guide.s3.s3;

import io.koraframework.application.graph.Lifecycle;
import io.koraframework.common.annotation.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * Bucket administration is not part of the declarative {@code @S3} contract, so it goes through the
 * AWS SDK client that {@code s3-client-aws} publishes.
 */
@Component
public final class S3BucketInitializer implements Lifecycle {

    private final S3Client s3Client;
    private final S3UploadsConfig config;

    public S3BucketInitializer(S3Client s3Client, S3UploadsConfig config) {
        this.s3Client = s3Client;
        this.config = config;
    }

    @Override
    public void init() {
        var bucket = this.config.bucket();
        try {
            this.s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            this.s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    @Override
    public void release() {}
}
