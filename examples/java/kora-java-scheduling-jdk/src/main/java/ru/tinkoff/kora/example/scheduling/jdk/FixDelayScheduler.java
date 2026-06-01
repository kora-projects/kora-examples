package ru.tinkoff.kora.example.scheduling.jdk;

import java.time.temporal.ChronoUnit;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleWithFixedDelay;

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
