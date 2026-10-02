package io.koraframework.example.scheduling.quartz;

import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.quartz.annotation.ScheduleQuartzWithCron;

@Component
public final class CronScheduler {

    private int state = 0;

    @ScheduleQuartzWithCron("* * * ? * * *")
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
