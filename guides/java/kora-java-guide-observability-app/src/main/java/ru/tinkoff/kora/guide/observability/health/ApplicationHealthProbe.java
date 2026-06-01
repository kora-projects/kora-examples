package ru.tinkoff.kora.guide.observability.health;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.liveness.LivenessProbe;
import ru.tinkoff.kora.common.liveness.LivenessProbeFailure;

@Component
public final class ApplicationHealthProbe implements LivenessProbe {

    @Override
    public LivenessProbeFailure probe() {
        return null;
    }
}
