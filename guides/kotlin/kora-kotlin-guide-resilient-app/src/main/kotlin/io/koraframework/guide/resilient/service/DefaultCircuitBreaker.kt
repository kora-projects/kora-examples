package io.koraframework.guide.resilient.service

import io.koraframework.resilient.circuitbreaker.CircuitBreaker
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec

@CircuitBreakerSpec("resilient.circuitbreaker.default")
interface DefaultCircuitBreaker : CircuitBreaker
