package io.koraframework.guide.resilient.service

import io.koraframework.common.annotation.Component
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.resilient.circuitbreaker.CircuitBreakerPredicate

@Component
class CircuitBreakerFailurePredicate : CircuitBreakerPredicate {
    override fun name(): String = "RecordServerErrorsOnly"

    override fun test(throwable: Throwable): Boolean {
        return throwable !is HttpServerResponseException || throwable.code() >= 500
    }
}
