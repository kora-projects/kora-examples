package io.koraframework.guide.grpcclient.advanced.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}
