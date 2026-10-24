package io.koraframework.guide.s3.dto;

import java.util.Map;
import io.koraframework.json.common.annotation.Json;

@Json
public record ErrorResponse(String error, String message, Map<String, String> details) {

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, Map.of());
    }

    public static ErrorResponse of(String error, String message, Map<String, String> details) {
        return new ErrorResponse(error, message, details);
    }
}

