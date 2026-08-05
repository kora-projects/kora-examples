package io.koraframework.guide.validation.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record ValidationErrorDetails(String field, String message) {}
