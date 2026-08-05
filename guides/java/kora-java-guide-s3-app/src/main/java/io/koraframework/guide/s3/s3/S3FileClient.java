package io.koraframework.guide.s3.s3;

import io.koraframework.s3.client.annotation.S3;
import io.koraframework.s3.client.model.S3Body;
import io.koraframework.s3.client.model.S3Object;
import io.koraframework.s3.client.model.S3ObjectList;
import io.koraframework.s3.client.model.S3ObjectUpload;

@S3.Client("s3client.uploads")
public interface S3FileClient {

    @S3.Put("files/{fileId}")
    S3ObjectUpload uploadFile(String fileId, S3Body body);

    @S3.Get("files/{fileId}")
    S3Object downloadFile(String fileId);

    @S3.List("files/")
    S3ObjectList listFiles();

    @S3.Delete("files/{fileId}")
    void deleteFile(String fileId);
}