package io.koraframework.example.submodule.vet.service;

import io.koraframework.resilient.circuitbreaker.CircuitBreaker;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec;

@CircuitBreakerSpec("resilient.circuitbreaker.vet")
public interface VetCircuitBreaker extends CircuitBreaker {

}
