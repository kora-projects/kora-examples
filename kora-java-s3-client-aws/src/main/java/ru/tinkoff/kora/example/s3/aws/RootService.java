package ru.tinkoff.kora.example.s3.aws;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public class RootService {

    private final SyncS3Client syncS3Client;
    private final AsyncS3Client asyncS3Client;
    private final AwsS3Client awsS3Client;

    public RootService(SyncS3Client syncS3Client, AsyncS3Client asyncS3Client, AwsS3Client awsS3Client) {
        this.syncS3Client = syncS3Client;
        this.asyncS3Client = asyncS3Client;
        this.awsS3Client = awsS3Client;
    }
}
