package io.koraframework.example.json;

import io.koraframework.json.common.annotation.Json;

@Json
public enum Status {
    CREATED(1), DELETED(2);

    private final int code;

    Status(int code) {
        this.code = code;
    }

    @Json
    public int code() {
        return this.code;
    }
}
