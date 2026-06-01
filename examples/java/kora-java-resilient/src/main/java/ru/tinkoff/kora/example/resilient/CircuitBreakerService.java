package ru.tinkoff.kora.example.resilient;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker;

@Component
public class CircuitBreakerService {

    @CircuitBreaker("my_cb")
    public String getSuccessful() {
        return "OK";
    }
}
