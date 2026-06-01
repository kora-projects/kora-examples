package ru.tinkoff.kora.guide.s3.s3;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record FileMetadata(String fileId, Long size, String contentType) {}