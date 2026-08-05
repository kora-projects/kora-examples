package io.koraframework.kotlin.example.resilient

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreaker
import io.koraframework.resilient.fallback.annotation.Fallback
import io.koraframework.resilient.retry.annotation.Retry
import io.koraframework.resilient.timeout.annotation.Timeout
import java.util.concurrent.ThreadLocalRandom

@Component
open class FallbackService {
    @Fallback(value = "my_fallback", method = "getFallback()")
    open fun getValue(fail: Boolean): String {
        if (fail) {
            throw IllegalStateException("Failed")
        }
        return VALUE
    }

    protected open fun getFallback(): String = FALLBACK

    companion object {
        const val VALUE = "OK"
        const val FALLBACK = "FALLBACK"
    }
}

