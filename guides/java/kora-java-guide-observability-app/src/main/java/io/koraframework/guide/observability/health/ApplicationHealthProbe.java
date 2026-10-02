package io.koraframework.guide.observability.health;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.liveness.LivenessProbe;
import io.koraframework.common.liveness.LivenessProbeFailure;

@Component
public final class ApplicationHealthProbe implements LivenessProbe {

    @Override
    public LivenessProbeFailure probe() {
        return null;
    }
}
