package io.koraframework.guide.databasejdbc.advanced.dto;

import io.koraframework.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}
