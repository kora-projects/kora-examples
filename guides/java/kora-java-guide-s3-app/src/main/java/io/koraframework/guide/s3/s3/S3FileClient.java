package io.koraframework.guide.s3.s3;

import io.koraframework.s3.client.kora.annotation.S3;
import io.koraframework.s3.client.kora.model.response.GetObjectResult;
import io.koraframework.s3.client.kora.model.response.ListBucketResult;

@S3.Client("s3client.uploads")
@S3.Bucket(".bucket")
public interface S3FileClient {

    @S3.Put("files/{fileId}")
    String uploadFile(String fileId, byte[] body);

    @S3.Get("files/{fileId}")
    GetObjectResult downloadFile(String fileId);

    @S3.List("files/")
    ListBucketResult listFiles();

    @S3.Delete("files/{fileId}")
    void deleteFile(String fileId);
}
