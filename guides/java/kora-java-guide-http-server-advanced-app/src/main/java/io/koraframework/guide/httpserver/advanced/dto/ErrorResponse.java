package io.koraframework.guide.httpserver.advanced.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record ErrorResponse(String message) {}
