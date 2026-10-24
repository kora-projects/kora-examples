package io.koraframework.example.scheduling.quartz;

import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.quartz.annotation.ScheduleQuartzWithTrigger;

@Component
public final class TriggerScheduler {

    private int state = 0;

    /**
     * @see Application#myTrigger()
     */
    @ScheduleQuartzWithTrigger(TriggerScheduler.class)
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
