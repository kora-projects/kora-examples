package io.koraframework.example.scheduling.jdk;

import java.time.temporal.ChronoUnit;
import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.jdk.annotation.ScheduleWithFixedDelay;

@Component
public final class FixDelayScheduler {

    private int state = 0;

    @ScheduleWithFixedDelay(initialDelay = 50, delay = 50, unit = ChronoUnit.MILLIS)
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
