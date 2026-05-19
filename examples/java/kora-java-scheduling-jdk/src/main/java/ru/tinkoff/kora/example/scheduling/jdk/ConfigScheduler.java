package ru.tinkoff.kora.example.scheduling.jdk;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleAtFixedRate;

@Component
public final class ConfigScheduler {

    private int state = 0;

    @ScheduleAtFixedRate(config = "scheduling.jobs.fix-rate")
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
