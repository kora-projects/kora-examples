package io.koraframework.guide.observability.health;

import java.time.Duration;
import java.time.Instant;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.readiness.ReadinessProbe;
import io.koraframework.common.readiness.ReadinessProbeFailure;

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
