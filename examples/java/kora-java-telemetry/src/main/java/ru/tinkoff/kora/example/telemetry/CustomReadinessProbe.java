package ru.tinkoff.kora.example.telemetry;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.readiness.ReadinessProbe;
import ru.tinkoff.kora.common.readiness.ReadinessProbeFailure;

@Component
public final class CustomReadinessProbe implements ReadinessProbe {

    @Override
    public ReadinessProbeFailure probe() {
        return new ReadinessProbeFailure("Error");
    }
}
