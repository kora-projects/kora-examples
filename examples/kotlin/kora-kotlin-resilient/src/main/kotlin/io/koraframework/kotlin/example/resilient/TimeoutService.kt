package io.koraframework.kotlin.example.resilient

import io.koraframework.common.annotation.Component
import io.koraframework.resilient.timeout.annotation.Timeout
import java.util.concurrent.ThreadLocalRandom

@Component
open class TimeoutService {
    @Timeout(DefaultTimeouter::class)
    open fun getSuccessful(): String = "OK"
}
