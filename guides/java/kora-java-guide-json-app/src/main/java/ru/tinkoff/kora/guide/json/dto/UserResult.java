package ru.tinkoff.kora.guide.json.dto;

import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.json.common.annotation.JsonDiscriminatorField;
import ru.tinkoff.kora.json.common.annotation.JsonDiscriminatorValue;

@Json
@JsonDiscriminatorField("status")
public sealed interface UserResult permits UserResult.UserSuccess, UserResult.UserError {

    @Json
    enum Status {
        OK,
        ERROR
    }

    Status status();

    @Json
    @JsonDiscriminatorValue("OK")
    record UserSuccess(Status status, UserResponse user) implements UserResult {}

    @Json
    @JsonDiscriminatorValue("ERROR")
    record UserError(Status status, String message) implements UserResult {}
}
