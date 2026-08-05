package io.koraframework.kotlin.example.config.hocon

import io.koraframework.config.common.annotation.ConfigSource
import io.koraframework.config.common.annotation.ConfigMapper
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.UUID
import java.util.Properties
import java.util.regex.Pattern

@ConfigSource("foo")
interface FooConfig {
    enum class EnumValue {
        ANY,
        SOME
    }

    fun valueEnvRequired(): String

    fun valueEnvOptional(): String?

    fun valueEnvDefault(): String
    fun valueRef(): String
    fun valueString(): String
    fun valueUuid(): UUID
    fun valuePattern(): Pattern
    fun valueEnum(): EnumValue
    fun valueLocalDate(): LocalDate
    fun valueLocalTime(): LocalTime
    fun valueLocalDateTime(): LocalDateTime
    fun valueOffsetTime(): OffsetTime
    fun valueOffsetDateTime(): OffsetDateTime
    fun valuePeriodAsInt(): Period
    fun valuePeriodAsString(): Period
    fun valueDuration(): Duration
    fun valueInt(): Int
    fun valueLong(): Long
    fun valueBigInt(): BigInteger
    fun valueDouble(): Double
    fun valueBigDecimal(): BigDecimal
    fun valueBoolean(): Boolean
    fun valueListAsString(): List<String>
    fun valueListAsArray(): List<String>
    fun valueSetAsString(): Set<String>
    fun valueSetAsArray(): Set<String>
    fun valueMap(): Map<String, String>
    fun valueProperties(): Properties

    @ConfigMapper
    interface BarConfig {
        fun someBarString(): String
        fun baz(): BazConfig

        @ConfigMapper
        interface BazConfig {
            fun someBazString(): String
        }
    }

    fun bar(): BarConfig
    fun bars(): List<BarConfig>
}

