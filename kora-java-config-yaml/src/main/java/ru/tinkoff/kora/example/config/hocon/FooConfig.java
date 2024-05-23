package ru.tinkoff.kora.example.config.hocon;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import ru.tinkoff.kora.config.common.annotation.ConfigSource;

@ConfigSource("foo")
public interface FooConfig {

    String valueEnvRequired();

    @Nullable
    String valueEnvOptional();

    String valueEnvDefault();

    String valueRef();

    String valueString();

    UUID valueUuid();

    Pattern valuePattern();

    EnumValue valueEnum();

    LocalDate valueLocalDate();

    LocalTime valueLocalTime();

    LocalDateTime valueLocalDateTime();

    OffsetTime valueOffsetTime();

    OffsetDateTime valueOffsetDateTime();

    Period valuePeriodAsInt();

    Period valuePeriodAsString();

    Duration valueDuration();

    int valueInt();

    long valueLong();

    BigInteger valueBigInt();

    double valueDouble();

    BigDecimal valueBigDecimal();

    boolean valueBoolean();

    List<String> valueListAsString();

    List<String> valueListAsArray();

    Set<String> valueSetAsString();

    Set<String> valueSetAsArray();

    Map<String, String> valueMap();

    Properties valueProperties();

    BarConfig bar();

    enum EnumValue {

        ANY,
        SOME
    }

    interface BarConfig {

        String someBarString();

        BazConfig baz();

        interface BazConfig {

            String someBazString();
        }
    }
}
