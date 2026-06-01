package ru.tinkoff.kora.example.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.example.config.hocon.Application;
import ru.tinkoff.kora.example.config.hocon.FooConfig;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class HoconConfigTests {

    @TestComponent
    private FooConfig fooConfig;

    @Test
    void configFilled() {
        assertEquals("valueRequired", fooConfig.valueEnvRequired());
        assertNull(fooConfig.valueEnvOptional());
        assertEquals("someDefaultValue", fooConfig.valueEnvDefault());
        assertEquals("SomeStringOtherSomeString", fooConfig.valueRef());
        assertEquals("SomeString", fooConfig.valueString());
        assertEquals(UUID.fromString("20684ccb-81f8-4fac-8ec0-297b08ff993d"), fooConfig.valueUuid());
        assertEquals(".*somePattern.*", fooConfig.valuePattern().pattern());
        assertEquals(FooConfig.EnumValue.ANY, fooConfig.valueEnum());
        assertEquals("2020-10-10", fooConfig.valueLocalDate().toString());
        assertEquals("12:10:10", fooConfig.valueLocalTime().toString());
        assertEquals("2020-10-10T12:10:10", fooConfig.valueLocalDateTime().toString());
        assertEquals("12:10:10+03:00", fooConfig.valueOffsetTime().toString());
        assertEquals("2020-10-10T12:10:10+03:00", fooConfig.valueOffsetDateTime().toString());
        assertEquals(Period.ofDays(1), fooConfig.valuePeriodAsInt());
        assertEquals(Period.ofDays(1), fooConfig.valuePeriodAsString());
        assertEquals(Duration.ofSeconds(250), fooConfig.valueDuration());
        assertEquals(1, fooConfig.valueInt());
        assertEquals(2L, fooConfig.valueLong());
        // assertEquals(BigInteger.valueOf(3), fooConfig.valueBigInt());
        assertEquals(4.1, fooConfig.valueDouble());
        // assertEquals(BigDecimal.valueOf(5.1), fooConfig.valueBigDecimal());
        assertTrue(fooConfig.valueBoolean());
        assertEquals(List.of("v1", "v2"), fooConfig.valueListAsString());
        assertEquals(List.of("v1", "v2"), fooConfig.valueListAsArray());
        assertEquals(Set.of("v1", "v2"), fooConfig.valueSetAsString());
        assertEquals(Map.of("k1", "v1", "k2", "v2"), fooConfig.valueMap());
        assertEquals(Map.of("k1", "v1", "k2", "v2"), fooConfig.valueProperties());
        assertEquals("someString", fooConfig.bar().someBarString());
        assertEquals("someString", fooConfig.bar().baz().someBazString());
        assertEquals("someString1", fooConfig.bars().get(0).someBarString());
        assertEquals("someString1", fooConfig.bars().get(0).baz().someBazString());
        assertEquals("someString2", fooConfig.bars().get(1).someBarString());
        assertEquals("someString2", fooConfig.bars().get(1).baz().someBazString());
    }
}
