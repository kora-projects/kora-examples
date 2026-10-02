package io.koraframework.example.json;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.json.common.annotation.JsonDiscriminatorField;
import io.koraframework.json.common.annotation.JsonDiscriminatorValue;

@Json
@JsonDiscriminatorField("type")
public sealed interface Event {
    @JsonDiscriminatorValue("created")
    @Json
    record Created(String id) implements Event {}

    @JsonDiscriminatorValue({"deleted", "removed"})
    @Json
    record Deleted(String id, boolean permanent) implements Event {}
}
