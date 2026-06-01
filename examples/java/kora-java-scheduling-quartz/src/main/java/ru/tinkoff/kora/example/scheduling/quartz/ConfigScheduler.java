package ru.tinkoff.kora.example.scheduling.quartz;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.scheduling.quartz.ScheduleWithCron;

@Component
public final class ConfigScheduler {

    private int state = 0;

    @ScheduleWithCron(config = "scheduling.jobs.quartz.cron")
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
