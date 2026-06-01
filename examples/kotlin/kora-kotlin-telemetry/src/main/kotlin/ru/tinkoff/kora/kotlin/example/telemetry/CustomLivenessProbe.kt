package ru.tinkoff.kora.kotlin.example.telemetry

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.liveness.LivenessProbe
import ru.tinkoff.kora.common.liveness.LivenessProbeFailure
import ru.tinkoff.kora.common.readiness.ReadinessProbe
import ru.tinkoff.kora.common.readiness.ReadinessProbeFailure
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.annotation.HttpController

@Component
class CustomLivenessProbe : LivenessProbe {
    override fun probe(): LivenessProbeFailure = LivenessProbeFailure("Error")
}

