package io.koraframework.guide.s3.s3

import io.koraframework.application.graph.Lifecycle
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException

/**
 * Bucket administration is not part of the declarative `@S3` contract, so it goes through the
 * AWS SDK client that `s3-client-aws` publishes.
 */
@Root
@Component
class S3BucketInitializer(
    private val s3Client: S3Client,
    private val config: S3UploadsConfig
) : Lifecycle {

    override fun init() {
        val bucket = config.bucket()
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
        } catch (e: NoSuchBucketException) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
        }
    }

    override fun release() {}
}
