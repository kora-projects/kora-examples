package ru.tinkoff.kora.guide.observability.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.Callable;
import ru.tinkoff.kora.common.Component;

@Component
public final class MetricsService {

    private final Counter userCreationCounter;
    private final Timer userCreationTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.userCreationCounter = Counter.builder("user.creation.total")
                .description("Total number of users created")
                .register(meterRegistry);
        this.userCreationTimer = Timer.builder("user.creation.duration")
                .description("Time taken to create users")
                .register(meterRegistry);
    }

    public <T> T recordUserCreation(Callable<T> action) {
        this.userCreationCounter.increment();
        try {
            return this.userCreationTimer.recordCallable(action);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to record user creation metrics", e);
        }
    }
}
