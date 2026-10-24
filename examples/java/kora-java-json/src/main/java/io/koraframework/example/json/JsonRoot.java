package io.koraframework.example.json;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import io.koraframework.json.common.JsonReader;
import io.koraframework.json.common.JsonWriter;

@Root
@Component
public final class JsonRoot {
    final JsonReader<JsonShowcase> showcaseReader;
    final JsonWriter<JsonShowcase> showcaseWriter;
    final JsonReader<Event> eventReader;
    final JsonWriter<Event> eventWriter;
    final JsonReader<OneWayModels.Input> inputReader;
    final JsonWriter<OneWayModels.Output> outputWriter;
    final JsonWriter<OneWayModels.RawPayload> rawPayloadWriter;

    public JsonRoot(
        JsonReader<JsonShowcase> showcaseReader,
        JsonWriter<JsonShowcase> showcaseWriter,
        JsonReader<Event> eventReader,
        JsonWriter<Event> eventWriter,
        JsonReader<OneWayModels.Input> inputReader,
        JsonWriter<OneWayModels.Output> outputWriter,
        JsonWriter<OneWayModels.RawPayload> rawPayloadWriter
    ) {
        this.showcaseReader = showcaseReader;
        this.showcaseWriter = showcaseWriter;
        this.eventReader = eventReader;
        this.eventWriter = eventWriter;
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
        this.rawPayloadWriter = rawPayloadWriter;
    }
}
