package ru.tinkoff.kora.example.telemetry;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.liveness.LivenessProbe;
import ru.tinkoff.kora.common.liveness.LivenessProbeFailure;

@Component
public final class CustomLivenessProbe implements LivenessProbe {

    @Override
    public LivenessProbeFailure probe() throws Exception {
        return new LivenessProbeFailure("Error");
    }
}
