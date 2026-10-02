package io.koraframework.example.scheduling.jdk;

import java.time.temporal.ChronoUnit;
import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkAtFixedRate;

@Component
public final class FixRateScheduler {

    private int state = 0;

    @ScheduleJdkAtFixedRate(initialDelay = 50, period = 50, unit = ChronoUnit.MILLIS)
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
