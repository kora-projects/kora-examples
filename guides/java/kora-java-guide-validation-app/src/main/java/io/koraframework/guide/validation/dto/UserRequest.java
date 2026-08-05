package io.koraframework.guide.validation.dto;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.validation.common.annotation.NotBlank;
import io.koraframework.validation.common.annotation.Pattern;
import io.koraframework.validation.common.annotation.Size;

@Json
public record UserRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Pattern("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") String email
) {}
