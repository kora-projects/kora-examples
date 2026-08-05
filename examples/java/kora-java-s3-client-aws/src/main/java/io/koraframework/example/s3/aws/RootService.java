package io.koraframework.example.s3.aws;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;

@Root
@Component
public class RootService {

    private final AwsS3Service awsS3Service;

    public RootService(AwsS3Service awsS3Service) {
        this.awsS3Service = awsS3Service;
    }
}
