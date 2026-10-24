package io.koraframework.example.config.hocon;

import org.jspecify.annotations.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import io.koraframework.config.common.annotation.ConfigSource;
import io.koraframework.config.common.annotation.ConfigMapper;
import io.koraframework.common.annotation.Mapping;

@ConfigSource("foo")
public interface FooConfig {

    enum EnumValue {
        ANY,
        SOME
    }

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

    @Mapping(TokenConfigValueMapper.class)
    Token apiToken();

    String relaxedKey();

    boolean valueBoolean();

    List<String> valueListAsString();

    List<String> valueListAsArray();

    Set<String> valueSetAsString();

    Set<String> valueSetAsArray();

    Map<String, String> valueMap();

    Properties valueProperties();

    @ConfigMapper
    interface BarConfig {

        String someBarString();

        BazConfig baz();

        @ConfigMapper
        interface BazConfig {

            String someBazString();
        }
    }

    BarConfig bar();

    List<BarConfig> bars();
}
