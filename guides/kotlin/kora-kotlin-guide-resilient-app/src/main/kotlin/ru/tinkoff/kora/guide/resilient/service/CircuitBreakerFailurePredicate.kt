package ru.tinkoff.kora.guide.resilient.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.resilient.circuitbreaker.CircuitBreakerPredicate

@Component
class CircuitBreakerFailurePredicate : CircuitBreakerPredicate {
    override fun name(): String = "RecordServerErrorsOnly"

    override fun test(throwable: Throwable): Boolean {
        return throwable !is HttpServerResponseException || throwable.code() >= 500
    }
}
