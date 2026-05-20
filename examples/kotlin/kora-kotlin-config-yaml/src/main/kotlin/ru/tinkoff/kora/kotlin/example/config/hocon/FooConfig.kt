package ru.tinkoff.kora.kotlin.example.config.hocon

import jakarta.annotation.Nullable
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import ru.tinkoff.kora.config.common.annotation.ConfigValueExtractor
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import java.util.regex.Pattern

@ConfigSource("foo")
interface FooConfig {
    enum class EnumValue {
        ANY,
        SOME
    }

    fun valueEnvRequired(): String

    @Nullable
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

    @ConfigValueExtractor
    interface BarConfig {
        fun someBarString(): String
        fun baz(): BazConfig

        @ConfigValueExtractor
        interface BazConfig {
            fun someBazString(): String
        }
    }

    fun bar(): BarConfig
    fun bars(): List<BarConfig>
}
