package ru.tinkoff.kora.guide.observability.health

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.readiness.ReadinessProbe
import ru.tinkoff.kora.common.readiness.ReadinessProbeFailure
import java.time.Duration
import java.time.Instant

@Component
class CustomReadinessProbe : ReadinessProbe {
    private val startedAt = Instant.now()

    override fun probe(): ReadinessProbeFailure? {
        val readyAt = startedAt.plus(Duration.ofMillis(500))
        return if (Instant.now().isBefore(readyAt)) {
            ReadinessProbeFailure("Service is warming up")
        } else {
            null
        }
    }
}
