package io.koraframework.kotlin.example.telemetry

import io.koraframework.common.annotation.Component
import io.koraframework.common.liveness.LivenessProbe
import io.koraframework.common.liveness.LivenessProbeFailure
import io.koraframework.common.readiness.ReadinessProbe
import io.koraframework.common.readiness.ReadinessProbeFailure
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.annotation.HttpController

@Component
class CustomReadinessProbe : ReadinessProbe {
    override fun probe(): ReadinessProbeFailure = ReadinessProbeFailure("Error")
}

