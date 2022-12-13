package ru.tinkoff.kora.example.scheduling.jdk;

import java.time.temporal.ChronoUnit;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleOnce;

@Component
public final class OnceScheduler {

    private int state = 0;

    @ScheduleOnce(delay = 50, unit = ChronoUnit.MILLIS)
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
