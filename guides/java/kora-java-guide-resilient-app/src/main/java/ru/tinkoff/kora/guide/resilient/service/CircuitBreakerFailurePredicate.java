package ru.tinkoff.kora.guide.resilient.service;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.resilient.circuitbreaker.CircuitBreakerPredicate;

@Component
public final class CircuitBreakerFailurePredicate implements CircuitBreakerPredicate {

    @Override
    public String name() {
        return "RecordServerErrorsOnly";
    }

    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof HttpServerResponseException exception) {
            return exception.code() >= 500;
        }
        return true;
    }
}

