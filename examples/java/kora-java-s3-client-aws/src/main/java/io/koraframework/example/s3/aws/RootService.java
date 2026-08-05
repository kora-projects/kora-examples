package io.koraframework.example.s3.aws;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;

@Root
@Component
public class RootService {

    private final SyncS3Client syncS3Client;
    private final AsyncS3Client asyncS3Client;
    private final ReactorS3Client reactorS3Client;
    private final AwsS3Client awsS3Client;

    public RootService(SyncS3Client syncS3Client,
                       AsyncS3Client asyncS3Client,
                       ReactorS3Client reactorS3Client,
                       AwsS3Client awsS3Client) {
        this.syncS3Client = syncS3Client;
        this.asyncS3Client = asyncS3Client;
        this.reactorS3Client = reactorS3Client;
        this.awsS3Client = awsS3Client;
    }
}
