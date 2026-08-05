package io.koraframework.guide.observability.health

import io.koraframework.common.annotation.Component
import io.koraframework.common.liveness.LivenessProbe
import io.koraframework.common.liveness.LivenessProbeFailure

@Component
class ApplicationHealthProbe : LivenessProbe {
    override fun probe(): LivenessProbeFailure? = null
}
