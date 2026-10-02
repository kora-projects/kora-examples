package io.koraframework.guide.json.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}
