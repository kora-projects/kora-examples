package io.koraframework.example.json;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.json.common.annotation.JsonDiscriminatorField;
import io.koraframework.json.common.annotation.JsonDiscriminatorValue;

@Json
@JsonDiscriminatorField("@type")
public sealed interface Response<T> {
    @JsonDiscriminatorValue("ok")
    @Json
    record Ok<T>(T data) implements Response<T> {}

    @JsonDiscriminatorValue("fail")
    @Json
    record Fail<T>(String error) implements Response<T> {}
}
