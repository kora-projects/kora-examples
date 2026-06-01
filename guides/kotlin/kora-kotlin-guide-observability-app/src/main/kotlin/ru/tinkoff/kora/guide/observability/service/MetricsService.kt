package ru.tinkoff.kora.guide.observability.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import ru.tinkoff.kora.common.Component
import java.util.concurrent.Callable

@Component
class MetricsService(
    meterRegistry: MeterRegistry
) {
    private val userCreationCounter: Counter = Counter.builder("user.creation.total")
        .description("Total number of users created")
        .register(meterRegistry)

    private val userCreationTimer: Timer = Timer.builder("user.creation.duration")
        .description("Time taken to create users")
        .register(meterRegistry)

    fun <T> recordUserCreation(action: Callable<T>): T {
        userCreationCounter.increment()
        return try {
            userCreationTimer.recordCallable(action)
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException("Failed to record user creation metrics", e)
        }
    }
}
