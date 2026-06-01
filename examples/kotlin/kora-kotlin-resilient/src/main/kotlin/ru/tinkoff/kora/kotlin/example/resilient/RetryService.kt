package ru.tinkoff.kora.kotlin.example.resilient

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker
import ru.tinkoff.kora.resilient.fallback.annotation.Fallback
import ru.tinkoff.kora.resilient.retry.annotation.Retry
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout
import java.util.concurrent.ThreadLocalRandom

@Component
open class RetryService {
    @Retry("custom1")
    open fun getValue(arg: String): String = arg
}

