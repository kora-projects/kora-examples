package io.koraframework.kotlin.example.resilient

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreaker
import io.koraframework.resilient.fallback.annotation.Fallback
import io.koraframework.resilient.retry.annotation.Retry
import io.koraframework.resilient.timeout.annotation.Timeout
import java.util.concurrent.ThreadLocalRandom

@Root
@Component
open class ComboService {
    @Fallback(method = "getFallback()")
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

