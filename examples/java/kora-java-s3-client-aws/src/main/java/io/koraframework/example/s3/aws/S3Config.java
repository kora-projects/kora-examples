package io.koraframework.example.s3.aws;

import io.koraframework.config.common.annotation.ConfigSource;

@ConfigSource("my")
public interface S3Config {

    String bucket();
}
