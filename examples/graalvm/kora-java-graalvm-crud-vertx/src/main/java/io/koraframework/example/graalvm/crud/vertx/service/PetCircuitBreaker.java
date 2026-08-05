package io.koraframework.example.graalvm.crud.vertx.service;

import io.koraframework.resilient.circuitbreaker.CircuitBreaker;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakerSpec;

@CircuitBreakerSpec("resilient.circuitbreaker.pet")
public interface PetCircuitBreaker extends CircuitBreaker {

}
