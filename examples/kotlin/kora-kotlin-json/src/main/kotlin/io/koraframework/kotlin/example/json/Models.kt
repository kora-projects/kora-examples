package io.koraframework.kotlin.example.json

import io.koraframework.common.annotation.Mapping
import io.koraframework.json.common.JsonNullable
import io.koraframework.json.common.JsonReader
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.RawJson
import io.koraframework.json.common.annotation.Json
import io.koraframework.json.common.annotation.JsonDiscriminatorField
import io.koraframework.json.common.annotation.JsonDiscriminatorValue
import io.koraframework.json.common.annotation.JsonField
import io.koraframework.json.common.annotation.JsonInclude
import io.koraframework.json.common.annotation.JsonSkip
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.exc.StreamReadException
import java.time.LocalDate
import java.util.UUID

@Json
data class JsonShowcase(
    @field:JsonField("identifier") val id: UUID,
    val optional: String?,
    @field:JsonSkip val internalOnly: Int?,
    @field:JsonInclude(JsonInclude.IncludeType.ALWAYS) val explicitNull: String?,
    val labels: List<String>,
    val priorities: Set<Int>,
    val dates: Map<String, LocalDate>,
    val patch: JsonNullable<String>,
    val status: Status,
    @Mapping(HexReader::class) @Mapping(HexWriter::class) val code: Int
) {
    class HexWriter : JsonWriter<Int> {
        override fun write(generator: JsonGenerator, value: Int?) {
            generator.writeString(value!!.toString(16))
        }
    }

    class HexReader : JsonReader<Int> {
        override fun read(parser: JsonParser): Int {
            if (parser.currentToken() != JsonToken.VALUE_STRING) {
                throw StreamReadException(parser, "Expected hexadecimal string")
            }
            return parser.valueAsString.toInt(16)
        }
    }
}

@Json
enum class Status(private val code: Int) {
    CREATED(1), DELETED(2);

    @Json
    fun code(): Int = code
}

@Json
@JsonDiscriminatorField("type")
sealed interface Event {
    @JsonDiscriminatorValue("created")
    @Json
    data class Created(val id: String) : Event

    @JsonDiscriminatorValue("deleted", "removed")
    @Json
    data class Deleted(val id: String, val permanent: Boolean) : Event
}

@Json
@JsonDiscriminatorField("@type")
sealed interface Response<T> {
    @JsonDiscriminatorValue("ok")
    @Json
    data class Ok<T>(val data: T) : Response<T>

    @JsonDiscriminatorValue("fail")
    @Json
    data class Fail<T>(val error: String) : Response<T>
}

@io.koraframework.json.common.annotation.JsonReader
data class Input(val value: String)

@io.koraframework.json.common.annotation.JsonWriter
data class Output(val value: String)

@io.koraframework.json.common.annotation.JsonWriter
data class RawPayload(val id: String, val payload: RawJson)
