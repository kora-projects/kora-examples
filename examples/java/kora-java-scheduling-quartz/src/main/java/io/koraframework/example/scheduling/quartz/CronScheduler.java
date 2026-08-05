package io.koraframework.example.scheduling.quartz;

import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.quartz.ScheduleWithCron;

@Component
public final class CronScheduler {

    private int state = 0;

    @ScheduleWithCron("* * * ? * * *")
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
