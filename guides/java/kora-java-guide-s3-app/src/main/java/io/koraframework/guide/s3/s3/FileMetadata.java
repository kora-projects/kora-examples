package io.koraframework.guide.s3.s3;

import io.koraframework.json.common.annotation.Json;

@Json
public record FileMetadata(String fileId, Long size, String contentType) {}