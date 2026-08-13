package io.koraframework.kotlin.example.submodule.pet.service

import io.koraframework.resilient.circuitbreaker.CircuitBreaker
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec

@CircuitBreakerSpec("resilient.circuitbreaker.pet")
interface PetCircuitBreaker : CircuitBreaker
