package ru.tinkoff.kora.guide.observability.health

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.liveness.LivenessProbe
import ru.tinkoff.kora.common.liveness.LivenessProbeFailure

@Component
class ApplicationHealthProbe : LivenessProbe {
    override fun probe(): LivenessProbeFailure? = null
}
