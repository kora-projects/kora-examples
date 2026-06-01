package ru.tinkoff.kora.guide.observability.health;

import java.time.Duration;
import java.time.Instant;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.readiness.ReadinessProbe;
import ru.tinkoff.kora.common.readiness.ReadinessProbeFailure;

@Component
public final class CustomReadinessProbe implements ReadinessProbe {

    private static final Duration WARMUP_PERIOD = Duration.ofMillis(500);

    private final Instant startedAt = Instant.now();

    @Override
    public ReadinessProbeFailure probe() {
        var readyAt = startedAt.plus(WARMUP_PERIOD);
        if (Instant.now().isBefore(readyAt)) {
            return new ReadinessProbeFailure("Service is warming up");
        }
        return null;
    }
}
