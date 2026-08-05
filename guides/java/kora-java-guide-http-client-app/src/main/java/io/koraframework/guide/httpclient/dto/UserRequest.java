package io.koraframework.guide.httpclient.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}
