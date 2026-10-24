package io.koraframework.guide.resilient.service;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.resilient.circuitbreaker.CircuitBreakerPredicate;

@Tag(DefaultCircuitBreaker.class)
@Component
public final class CircuitBreakerFailurePredicate implements CircuitBreakerPredicate {

    @Override
    public boolean isCircuitBreakerFailure(Throwable throwable) {
        if (throwable instanceof HttpServerResponseException exception) {
            return exception.code() >= 500;
        }
        return true;
    }
}
