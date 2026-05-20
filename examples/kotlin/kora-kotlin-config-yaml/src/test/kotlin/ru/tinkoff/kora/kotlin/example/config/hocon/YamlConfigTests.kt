package ru.tinkoff.kora.kotlin.example.config.hocon

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Duration
import java.time.Period
import java.util.*

@KoraAppTest(Application::class)
class YamlConfigTests {

    @TestComponent
    lateinit var fooConfig: FooConfig

    @Test
    fun configFilled() {
        assertEquals("valueRequired", fooConfig.valueEnvRequired())
        assertNull(fooConfig.valueEnvOptional())
        assertEquals("someDefaultValue", fooConfig.valueEnvDefault())
        assertEquals("SomeStringOtherSomeString", fooConfig.valueRef())
        assertEquals("SomeString", fooConfig.valueString())
        assertEquals(UUID.fromString("20684ccb-81f8-4fac-8ec0-297b08ff993d"), fooConfig.valueUuid())
        assertEquals(".*somePattern.*", fooConfig.valuePattern().pattern())
        assertEquals(FooConfig.EnumValue.ANY, fooConfig.valueEnum())
        assertEquals("2020-10-10", fooConfig.valueLocalDate().toString())
        assertEquals("12:10:10", fooConfig.valueLocalTime().toString())
        assertEquals("2020-10-10T12:10:10", fooConfig.valueLocalDateTime().toString())
        assertEquals("12:10:10+03:00", fooConfig.valueOffsetTime().toString())
        assertEquals("2020-10-10T12:10:10+03:00", fooConfig.valueOffsetDateTime().toString())
        assertEquals(Period.ofDays(1), fooConfig.valuePeriodAsInt())
        assertEquals(Period.ofDays(1), fooConfig.valuePeriodAsString())
        assertEquals(Duration.ofSeconds(250), fooConfig.valueDuration())
        assertEquals(1, fooConfig.valueInt())
        assertEquals(2L, fooConfig.valueLong())
        assertEquals(4.1, fooConfig.valueDouble())
        assertTrue(fooConfig.valueBoolean())
        assertEquals(listOf("v1", "v2"), fooConfig.valueListAsString())
        assertEquals(listOf("v1", "v2"), fooConfig.valueListAsArray())
        assertEquals(setOf("v1", "v2"), fooConfig.valueSetAsString())
        assertEquals(mapOf("k1" to "v1", "k2" to "v2"), fooConfig.valueMap())
        assertEquals(mapOf("k1" to "v1", "k2" to "v2"), fooConfig.valueProperties())
        assertEquals("someString", fooConfig.bar().someBarString())
        assertEquals("someString", fooConfig.bar().baz().someBazString())
        assertEquals("someString1", fooConfig.bars()[0].someBarString())
        assertEquals("someString1", fooConfig.bars()[0].baz().someBazString())
        assertEquals("someString2", fooConfig.bars()[1].someBarString())
        assertEquals("someString2", fooConfig.bars()[1].baz().someBazString())
    }
}
