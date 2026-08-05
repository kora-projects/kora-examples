package io.koraframework.guide.resilient.service;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.resilient.circuitbreaker.CircuitBreakerPredicate;

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

