package io.koraframework.guide.observability.health

import io.koraframework.common.annotation.Component
import io.koraframework.common.readiness.ReadinessProbe
import io.koraframework.common.readiness.ReadinessProbeFailure
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
