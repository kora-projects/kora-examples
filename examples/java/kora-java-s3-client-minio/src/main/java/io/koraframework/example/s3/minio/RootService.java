package io.koraframework.example.s3.minio;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;

@Root
@Component
public class RootService {

    private final SyncS3Client syncS3Client;

    public RootService(SyncS3Client syncS3Client) {
        this.syncS3Client = syncS3Client;
    }
}
