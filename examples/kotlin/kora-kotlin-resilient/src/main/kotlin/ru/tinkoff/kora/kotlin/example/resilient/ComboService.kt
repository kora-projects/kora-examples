package ru.tinkoff.kora.kotlin.example.resilient

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker
import ru.tinkoff.kora.resilient.fallback.annotation.Fallback
import ru.tinkoff.kora.resilient.retry.annotation.Retry
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout
import java.util.concurrent.ThreadLocalRandom

@Root
@Component
open class ComboService {
    @Fallback(value = "my_fallback", method = "getFallback()")
    @CircuitBreaker("my_cb")
    @Retry("my_retry")
    @Timeout("my_timeout")
    open fun getValue(fail: Boolean): String {
        if (fail) {
            throw IllegalStateException("Failed")
        }

        try {
            val emulateWork = ThreadLocalRandom.current().nextInt(100, 1000)
            Thread.sleep(emulateWork.toLong())
            return VALUE
        } catch (e: InterruptedException) {
            throw IllegalStateException(e)
        }
    }

    protected open fun getFallback(): String = FALLBACK

    companion object {
        const val VALUE = "OK"
        const val FALLBACK = "FALLBACK"
    }
}

