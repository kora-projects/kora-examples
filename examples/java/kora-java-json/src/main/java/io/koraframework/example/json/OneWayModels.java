package io.koraframework.example.json;

import io.koraframework.json.common.annotation.JsonReader;
import io.koraframework.json.common.annotation.JsonWriter;
import io.koraframework.json.common.RawJson;

public final class OneWayModels {
    private OneWayModels() {}

    @JsonReader
    public record Input(String value) {}

    @JsonWriter
    public record Output(String value) {}

    @JsonWriter
    public record RawPayload(String id, RawJson payload) {}
}
