package io.koraframework.example.scheduling.jdk;

import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.jdk.annotation.ScheduleAtFixedRate;

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
