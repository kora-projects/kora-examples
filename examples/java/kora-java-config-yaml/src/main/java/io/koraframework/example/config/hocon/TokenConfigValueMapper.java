package io.koraframework.example.config.hocon;

import io.koraframework.common.annotation.Component;
import io.koraframework.config.common.ConfigValue;
import io.koraframework.config.common.mapper.ConfigValueMapper;

@Component
public final class TokenConfigValueMapper implements ConfigValueMapper<Token> {
    @Override
    public Token map(ConfigValue<?> value) {
        if (value instanceof ConfigValue.NullValue) return null;
        var raw = value.asString();
        if (!raw.startsWith("Bearer ")) throw new IllegalArgumentException("Token must start with 'Bearer '");
        return new Token(raw.substring("Bearer ".length()));
    }
}
