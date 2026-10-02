package io.koraframework.kotlin.example.submodule.vet.service

import io.koraframework.resilient.circuitbreaker.CircuitBreaker
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec

@CircuitBreakerSpec("resilient.circuitbreaker.vet")
interface VetCircuitBreaker : CircuitBreaker
