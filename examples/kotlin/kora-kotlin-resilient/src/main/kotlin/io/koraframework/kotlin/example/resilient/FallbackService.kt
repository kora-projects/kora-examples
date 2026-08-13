package io.koraframework.kotlin.example.resilient

import io.koraframework.common.annotation.Component
import io.koraframework.resilient.fallback.annotation.Fallback
import java.util.concurrent.ThreadLocalRandom

@Component
open class FallbackService {
    @Fallback(method = "getFallback()")
    open fun getValue(fail: Boolean): String {
        if (fail) {
            throw IllegalStateException("Failed")
        }
        return VALUE
    }

    protected open fun getFallback(): String = FALLBACK

    companion object {
        const val VALUE = "OK"
        const val FALLBACK = "FALLBACK"
    }
}
