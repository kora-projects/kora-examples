package io.koraframework.example.telemetry;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.readiness.ReadinessProbe;
import io.koraframework.common.readiness.ReadinessProbeFailure;

@Component
public final class CustomReadinessProbe implements ReadinessProbe {

    @Override
    public ReadinessProbeFailure probe() {
        return new ReadinessProbeFailure("Error");
    }
}
