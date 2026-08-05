package io.koraframework.kotlin.example.resilient

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreaker
import io.koraframework.resilient.fallback.annotation.Fallback
import io.koraframework.resilient.retry.annotation.Retry
import io.koraframework.resilient.timeout.annotation.Timeout
import java.util.concurrent.ThreadLocalRandom

@Component
open class RetryService {
    @Retry("custom1")
    open fun getValue(arg: String): String = arg
}

