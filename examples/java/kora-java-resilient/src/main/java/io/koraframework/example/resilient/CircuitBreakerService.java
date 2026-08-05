package io.koraframework.example.resilient;

import io.koraframework.common.annotation.Component;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable;

@Component
public class CircuitBreakerService {

    @CircuitBreakable(MyCircuitBreaker.class)
    public String getSuccessful() {
        return "OK";
    }
}
