package io.koraframework.kotlin.example.resilient

import io.koraframework.common.annotation.Component
import io.koraframework.resilient.retry.annotation.Retryable
import java.util.concurrent.ThreadLocalRandom

@Component
open class RetryService {
    @Retryable(DefaultRetry::class)
    open fun getValue(arg: String): String = arg
}
