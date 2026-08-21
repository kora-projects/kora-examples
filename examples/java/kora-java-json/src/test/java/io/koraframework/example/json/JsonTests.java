package io.koraframework.example.json;

import io.koraframework.json.common.JsonNullable;
import io.koraframework.json.common.JsonReader;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.RawJson;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@KoraAppTest(Application.class)
class JsonTests {
    @TestComponent JsonRoot root;

    @Test
    void roundTripCoversFieldRulesBuiltInsRawJsonAndMapping() throws Exception {
        var value = new JsonShowcase(
            UUID.fromString("20684ccb-81f8-4fac-8ec0-297b08ff993d"), null, 42, null,
            List.of("json", "kora"), Set.of(1, 2), Map.of("release", LocalDate.of(2026, 8, 14)),
            JsonNullable.of("changed"), Status.CREATED, 255
        );

        var json = root.showcaseWriter.toString(value);
        assertTrue(json.contains("\"identifier\""));
        assertTrue(json.contains("\"explicitNull\":null"));
        assertTrue(json.contains("\"code\":\"ff\""));
        assertFalse(json.contains("internalOnly"));
        assertEquals(255, root.showcaseReader.read(json).code());
    }

    @Test
    void supportsSealedGenericAndOneWayCodecs() throws Exception {
        Event event = new Event.Deleted("42", true);
        assertEquals(event, root.eventReader.read(root.eventWriter.toString(event)));

        assertEquals("input", root.inputReader.read("{\"value\":\"input\"}").value());
        assertEquals("{\"value\":\"output\"}", root.outputWriter.toString(new OneWayModels.Output("output")));
        assertEquals("{\"id\":\"1\",\"payload\":{\"trusted\":true}}",
            root.rawPayloadWriter.toString(new OneWayModels.RawPayload("1", new RawJson("{\"trusted\":true}"))));
    }
}
