package ru.tinkoff.kora.guide.s3.s3;

import ru.tinkoff.kora.application.graph.Lifecycle;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.s3.client.S3ClientConfig;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@Component
public final class S3BucketInitializer implements Lifecycle {

    private final S3Client s3Client;
    private final S3ClientConfig s3ClientConfig;

    public S3BucketInitializer(S3Client s3Client, @Tag(S3FileClient.class) S3ClientConfig s3ClientConfig) {
        this.s3Client = s3Client;
        this.s3ClientConfig = s3ClientConfig;
    }

    @Override
    public void init() {
        var bucket = this.s3ClientConfig.bucket();
        try {
            this.s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            this.s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    @Override
    public void release() {}
}