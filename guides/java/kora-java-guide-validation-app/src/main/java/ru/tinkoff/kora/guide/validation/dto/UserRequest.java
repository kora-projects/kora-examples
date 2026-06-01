package ru.tinkoff.kora.guide.validation.dto;

import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.validation.common.annotation.NotBlank;
import ru.tinkoff.kora.validation.common.annotation.Pattern;
import ru.tinkoff.kora.validation.common.annotation.Size;

@Json
public record UserRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Pattern("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") String email
) {}
