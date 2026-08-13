package io.koraframework.kotlin.example.resilient

import io.koraframework.resilient.circuitbreaker.CircuitBreaker
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec

@CircuitBreakerSpec("resilient.circuitbreaker.my_cb")
interface MyCircuitBreaker : CircuitBreaker
