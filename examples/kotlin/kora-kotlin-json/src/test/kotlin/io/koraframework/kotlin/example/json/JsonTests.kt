package io.koraframework.kotlin.example.json

import io.koraframework.json.common.JsonNullable
import io.koraframework.json.common.JsonReader
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.RawJson
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

@KoraAppTest(Application::class)
class JsonTests {
    @TestComponent lateinit var root: JsonRoot

    @Test
    fun roundTripCoversFieldRulesBuiltInsRawJsonAndMapping() {
        val value = JsonShowcase(
            UUID.fromString("20684ccb-81f8-4fac-8ec0-297b08ff993d"), null, 42, null,
            listOf("json", "kora"), setOf(1, 2), mapOf("release" to LocalDate.of(2026, 8, 14)),
            JsonNullable.of("changed"), Status.CREATED, 255
        )

        val json = root.showcaseWriter.toString(value)
        assertTrue(json.contains("\"identifier\""))
        assertTrue(json.contains("\"explicitNull\":null"))
        assertTrue(json.contains("\"code\":\"ff\""))
        assertFalse(json.contains("internalOnly"))
        assertEquals(255, root.showcaseReader.read(json)!!.code)
    }

    @Test
    fun supportsSealedGenericAndOneWayCodecs() {
        val event: Event = Event.Deleted("42", true)
        assertEquals(event, root.eventReader.read(root.eventWriter.toString(event)))

        assertEquals("input", root.inputReader.read("{\"value\":\"input\"}")!!.value)
        assertEquals("{\"value\":\"output\"}", root.outputWriter.toString(Output("output")))
        assertEquals("{\"id\":\"1\",\"payload\":{\"trusted\":true}}",
            root.rawPayloadWriter.toString(RawPayload("1", RawJson("""{"trusted":true}"""))))
    }
}
