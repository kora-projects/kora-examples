package io.koraframework.example.json;

import io.koraframework.common.annotation.Mapping;
import io.koraframework.json.common.JsonNullable;
import io.koraframework.json.common.JsonReader;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.json.common.annotation.JsonField;
import io.koraframework.json.common.annotation.JsonInclude;
import io.koraframework.json.common.annotation.JsonSkip;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.exc.StreamReadException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.koraframework.json.common.annotation.JsonInclude.IncludeType.ALWAYS;

@Json
public record JsonShowcase(
    @JsonField("identifier") UUID id,
    @Nullable String optional,
    @JsonSkip @Nullable Integer internalOnly,
    @JsonInclude(ALWAYS) @Nullable String explicitNull,
    List<String> labels,
    Set<Integer> priorities,
    Map<String, LocalDate> dates,
    JsonNullable<String> patch,
    Status status,
    @Mapping(HexReader.class) @Mapping(HexWriter.class) Integer code
) {
    public static final class HexWriter implements JsonWriter<Integer> {
        @Override
        public void write(JsonGenerator generator, Integer value) {
            generator.writeString(Integer.toHexString(value));
        }
    }

    public static final class HexReader implements JsonReader<Integer> {
        @Override
        public Integer read(JsonParser parser) {
            if (parser.currentToken() != JsonToken.VALUE_STRING) {
                throw new StreamReadException(parser, "Expected hexadecimal string");
            }
            return Integer.parseInt(parser.getValueAsString(), 16);
        }
    }
}
