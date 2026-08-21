package io.koraframework.kotlin.example.config.hocon

import io.koraframework.common.annotation.Component
import io.koraframework.config.common.ConfigValue
import io.koraframework.config.common.mapper.ConfigValueMapper

data class Token(val value: String)

@Component
class TokenConfigValueMapper : ConfigValueMapper<Token> {
    override fun map(value: ConfigValue<*>): Token? {
        if (value is ConfigValue.NullValue) return null
        return Token(value.asString().removePrefix("Bearer ").also {
            require(it != value.asString()) { "Token must start with 'Bearer '" }
        })
    }
}
