package ru.tinkoff.kora.example.scheduling.jdk;

import java.time.temporal.ChronoUnit;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleAtFixedRate;

@Component
public final class FixRateScheduler {

    private int state = 0;

    @ScheduleAtFixedRate(initialDelay = 50, period = 50, unit = ChronoUnit.MILLIS)
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
