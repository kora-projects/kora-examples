package io.koraframework.guide.messaging.kafka.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record UserAcceptedResponse(String id) {}
