package io.koraframework.guide.resilient.service

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.resilient.circuitbreaker.CircuitBreakerPredicate

// 2.0 selects the predicate by the circuit breaker spec tag instead of by a name() the aspect looked up
@Tag(DefaultCircuitBreaker::class)
@Component
class CircuitBreakerFailurePredicate : CircuitBreakerPredicate {

    override fun isCircuitBreakerFailure(throwable: Throwable): Boolean {
        if (throwable is HttpServerResponseException) {
            return throwable.code() >= 500
        }
        return true
    }
}
