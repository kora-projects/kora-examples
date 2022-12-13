package ru.tinkoff.kora.example.scheduling.quartz;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.scheduling.quartz.ScheduleWithCron;

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
