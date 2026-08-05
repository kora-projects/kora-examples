package io.koraframework.kotlin.example.resilient

import io.koraframework.common.annotation.Component
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable
import java.util.concurrent.ThreadLocalRandom

@Component
open class CircuitBreakerService {
    @CircuitBreakable(MyCircuitBreaker::class)
    open fun getSuccessful(): String = "OK"
}
