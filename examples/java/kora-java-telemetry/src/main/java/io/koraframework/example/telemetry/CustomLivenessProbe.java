package io.koraframework.example.telemetry;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.liveness.LivenessProbe;
import io.koraframework.common.liveness.LivenessProbeFailure;

@Component
public final class CustomLivenessProbe implements LivenessProbe {

    @Override
    public LivenessProbeFailure probe() {
        return new LivenessProbeFailure("Error");
    }
}
