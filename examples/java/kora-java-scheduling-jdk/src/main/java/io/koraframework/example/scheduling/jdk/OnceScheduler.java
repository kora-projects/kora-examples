package io.koraframework.example.scheduling.jdk;

import java.time.temporal.ChronoUnit;
import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkOnce;

@Component
public final class OnceScheduler {

    private int state = 0;

    @ScheduleJdkOnce(delay = 50, unit = ChronoUnit.MILLIS)
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
