package io.koraframework.kotlin.example.json

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.json.common.JsonReader
import io.koraframework.json.common.JsonWriter

@Root
@Component
class JsonRoot(
    val showcaseReader: JsonReader<JsonShowcase>,
    val showcaseWriter: JsonWriter<JsonShowcase>,
    val eventReader: JsonReader<Event>,
    val eventWriter: JsonWriter<Event>,
    val inputReader: JsonReader<Input>,
    val outputWriter: JsonWriter<Output>,
    val rawPayloadWriter: JsonWriter<RawPayload>
)
