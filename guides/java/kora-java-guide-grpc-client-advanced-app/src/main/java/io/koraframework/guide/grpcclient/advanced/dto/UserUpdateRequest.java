package io.koraframework.guide.grpcclient.advanced.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record UserUpdateRequest(String userId, String name, String email) {}
